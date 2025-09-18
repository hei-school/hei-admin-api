package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import school.hei.haapi.http.mapper.TransactionDetailsMapper;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.repository.MobileTransactionDetailsRepository;
import school.hei.haapi.service.mobileMoney.MobileMoneyApi;
import school.hei.haapi.service.mobileMoney.MobileTransactionProvider;

@Slf4j
@Service
@AllArgsConstructor
public class MobilePaymentService implements MobileTransactionProvider {
  private final MobileTransactionDetailsRepository mobileTransactionDetailsRepository;
  private final TransactionDetailsMapper transactionDetailsMapper;
  private final MobileMoneyApi mobileMoneyApi;

  @Override
  public List<TransactionDetails> fetchTransactionDetails() {
    // TODO: for each mobile money type fetch transaction
    return mobileMoneyApi.fetchThenSaveTransactionsDetails(ORANGE_MONEY);
  }

  @Override
  public Optional<TransactionDetails> findTransactionByMpbs(Mpbs mpbs) {
    String transactionRef = mpbs.getPspId();
    return findTransactionByRef(transactionRef)
        .map(transactionDetailsMapper::toExternalTransactionDetails);
  }

  public List<MobileTransactionDetails> findAllTransactionByMpbs(List<Mpbs> mpbsList) {
    List<String> transactionRefs = mpbsList.stream().map(Mpbs::getPspId).toList();
    return findAllTransactionByRef(transactionRefs);
  }

  private List<MobileTransactionDetails> findAllTransactionByRef(List<String> transactionRefs) {
    return mobileTransactionDetailsRepository.findAllByPspTransactionRefIn(transactionRefs);
  }

  public MobileTransactionDetails getTransactionByRef(String pspId) {
    return mobileTransactionDetailsRepository
        .findByPspTransactionRef(pspId)
        .orElseThrow(
            () -> new NotFoundException("Mobile transaction with ref " + pspId + " not found"));
  }

  public Optional<MobileTransactionDetails> findTransactionByRef(String pspId) {
    return mobileTransactionDetailsRepository.findByPspTransactionRef(pspId);
  }

  public List<MobileTransactionDetails> saveAll(
      List<MobileTransactionDetails> mobileTransactionDetails) {
    List<MobileTransactionDetails> savedTransactions = new ArrayList<>();
    for (MobileTransactionDetails transaction : mobileTransactionDetails) {
      attemptSaveTransaction(transaction, savedTransactions);
    }
    return savedTransactions;
  }

  private void attemptSaveTransaction(
      MobileTransactionDetails transaction, List<MobileTransactionDetails> savedTransactions) {
    try {
      var saved = mobileTransactionDetailsRepository.save(transaction);
      savedTransactions.add(saved);
    } catch (DataIntegrityViolationException e) {
      log.info(
          "Error saving mobile transaction of ref {} because of error",
          transaction.getPspTransactionRef(),
          e);
    }
  }
}
