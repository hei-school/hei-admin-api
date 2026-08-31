package school.hei.haapi.service;

import static java.time.Instant.now;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CREDIT;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Credit;
import school.hei.haapi.model.CreditMovement;
import school.hei.haapi.model.CreditTransaction;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.CreditRepository;
import school.hei.haapi.repository.TransactionRepository;

@Service
@AllArgsConstructor
@Slf4j
public class CreditService {
  private final CreditRepository creditRepository;
  private final TransactionRepository transactionRepository;

  public Optional<Credit> getCreditByStudentId(String studentId) {
    return creditRepository.findCreditByStudent_Id(studentId);
  }

  public List<CreditTransaction> getCreditTransactionsByStudentId(
      String studentId, PageFromOne page, BoundedPageSize pageSize) {
    var pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    var credit = getCreditByStudentId(studentId);
    if (credit.isEmpty()) {
      return List.of();
    }
    return transactionRepository.findTransactionsByCredit_Id(credit.get().getId(), pageable);
  }

  public List<Credit> saveAll(List<Credit> credits) {
    return creditRepository.saveAll(credits);
  }

  public List<CreditTransaction> saveCreditTransactions(List<CreditTransaction> transactions) {
    return transactionRepository.saveAll(transactions);
  }

  public void depositArchivedFee(Fee fee) {
    if (fee.isArchived()) {
      throw new BadRequestException("Fee can't archived two times");
    }

    applyTransaction(
        getOrCreateCredit(fee.getStudent()),
        fee,
        null,
        fee.getTotalAmount(),
        CreditMovement.CREDIT);
  }

  public void subtractStudentCreditByPayment(Payment payment) {
    if (!isPaidByCredit(payment)) {
      return;
    }
    applyTransaction(
        getCreditByStudentId(payment.getFee().getStudent().getId()).orElseThrow(),
        payment.getFee(),
        payment,
        payment.getAmount(),
        CreditMovement.DEBIT);
  }

  public void transferFeeOverpaymentToCredit(Fee fee, User student) {
    var overpayment = -fee.getRemainingAmount();
    if (overpayment <= 0) {
      return;
    }
    if (transactionRepository.existsByFee_IdAndCreditMovement(fee.getId(), CreditMovement.CREDIT)) {
      log.warn(
          "A credit transaction already exists for fee {}, skipping duplicate overpayment"
              + " transfer of {}",
          fee.getId(),
          overpayment);
      fee.setRemainingAmount(0);
      return;
    }
    applyTransaction(getOrCreateCredit(student), fee, null, overpayment, CreditMovement.CREDIT);
    fee.setRemainingAmount(0);
  }

  private boolean isPaidByCredit(Payment payment) {
    return CREDIT.equals(payment.getType());
  }

  private Credit getOrCreateCredit(User student) {
    return getCreditByStudentId(student.getId())
        .orElseGet(
            () -> Credit.builder().student(student).amount(0).creationDatetime(now()).build());
  }

  private void applyTransaction(
      Credit credit, Fee fee, Payment payment, int amount, CreditMovement movement) {
    if (CreditMovement.CREDIT.equals(movement)) {
      credit.setAmount(credit.getAmount() + amount);
    } else {
      credit.setAmount(credit.getAmount() - amount);
    }
    var savedCredit = saveAll(List.of(credit)).getFirst();
    var transaction =
        CreditTransaction.builder()
            .credit(savedCredit)
            .fee(fee)
            .payment(payment)
            .amount(amount)
            .creditMovement(movement)
            .creationDatetime(now())
            .build();

    saveCreditTransactions(List.of(transaction));
  }
}
