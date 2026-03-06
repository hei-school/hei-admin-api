package school.hei.haapi.service;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.SUCCEEDED;

import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.http.mapper.TransactionDetailsMapper;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.dto.MobileTransactionDetailsDto;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsVerification;
import school.hei.haapi.model.psp.vola.api.VolaPsp;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.mapper.VolaMapper;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.MpbsVerificationRepository;
import school.hei.haapi.service.aws.FileService;
import school.hei.haapi.service.utils.CollectionUtils;
import school.hei.haapi.service.utils.excel.ExcelParser;

@Service
@AllArgsConstructor
@Slf4j
public class MpbsVerificationService {
  private final MpbsVerificationRepository repository;
  private final MpbsRepository mpbsRepository;
  private final MobilePaymentService mobilePaymentService;
  private final TransactionDetailsMapper transactionDetailsMapper;
  private final MultipartFileConverter multipartFileConverter;
  private final FileService fileService;
  private final UnverifiedMobilePaymentHandler unverifiedMobilePaymentHandler;
  private final ComputeVerifiedMobilePayment computeVerifiedMobilePayment;
  private final CollectionUtils collectionUtils;
  private final VolaPsp volaPsp;
  private final VolaMapper volaMapper;

  public List<MpbsVerification> findAllByStudentIdAndFeeId(String studentId, String feeId) {
    return repository.findAllByStudentIdAndFeeId(studentId, feeId);
  }

  public List<MpbsVerification> verifyMobilePaymentAndSaveResult(List<Mpbs> pendingMpbsList) {
    log.info(
        "Starting mobile payment verification for {} pending MPBS records", pendingMpbsList.size());
    var pendingMpbsCopy = List.copyOf(pendingMpbsList);

    List<Payment> paymentsRetrievedFromVola;

    try {
      paymentsRetrievedFromVola =
          volaPsp.getPayments(pendingMpbsCopy.stream().map(volaMapper::mpbsToPaymentIds).toList());
    } catch (RuntimeException e) {
      unverifiedMobilePaymentHandler.accept(pendingMpbsCopy);
      log.error(
          "Fatal error fetching payments from Vola for {} MPBS - marking all as unverified",
          pendingMpbsCopy.size(),
          e);
      return List.of();
    }
    log.info(
        "Retrieving payments from Vola PSP for {} payment IDs", paymentsRetrievedFromVola.size());
    var successPayments =
        paymentsRetrievedFromVola.stream()
            .filter(payment -> SUCCEEDED.equals(payment.getVerificationStatus()))
            .toList();
    log.info(
        "Found {} succeeded payments in {} payments",
        successPayments.size(),
        paymentsRetrievedFromVola.size());

    var successIdList =
        successPayments.stream().map(Payment::getPspPayment).map(PspPayment::getId).toList();

    var verifiedMpbs =
        pendingMpbsCopy.stream().filter(mpbs -> successIdList.contains(mpbs.getPspId())).toList();
    var unverifiedMpbs =
        pendingMpbsCopy.stream()
            .filter(Predicate.not(mpbs -> successIdList.contains(mpbs.getPspId())))
            .toList();

    unverifiedMobilePaymentHandler.accept(unverifiedMpbs);

    return saveVerifiedPayments(verifiedMpbs, successPayments);
  }

  private List<MpbsVerification> saveVerifiedPayments(
      List<Mpbs> mpbsList, List<Payment> volaPayments) {
    var result = new ArrayList<MpbsVerification>();

    for (Mpbs mpbs : mpbsList) {
      var associateVolaPayment =
          volaPayments.stream()
              .filter(payment -> mpbs.getPspId().equals(payment.getPspPayment().getId()))
              .findFirst()
              .get();
      var associateTransactionDetail =
          transactionDetailsMapper.fromVolaPayment(associateVolaPayment);
      result.add(
          computeVerifiedMobilePayment.saveTheVerifiedMpbs(mpbs, associateTransactionDetail));
    }

    return result;
  }

  @Transactional
  public List<Mpbs> computeFromXls(File file) throws IOException {
    List<String> pspToVerify = generateMobileTransactionDetailsFromXlsFile(file);
    List<Mpbs> mpbsToVerify = mpbsRepository.findByPspIdIn(pspToVerify);
    verifyMobilePaymentAndSaveResult(mpbsToVerify);
    return mpbsToVerify;
  }

  public String uploadXlsToS3(MultipartFile multipartFile) {
    String fileKey = "/XLS/" + multipartFile.getOriginalFilename();
    File file = multipartFileConverter.apply(multipartFile);
    fileService.uploadObjectToS3Bucket(fileKey, file);
    return fileKey;
  }

  private List<String> generateMobileTransactionDetailsFromXlsFile(File file) throws IOException {
    var excelParser =
        new ExcelParser<>(
            MobileTransactionDetailsDto.class, MobileTransactionDetailsDto.getExcelColumnMap());
    var transactions =
        excelParser.parseFile(file, 0, CREATE_NULL_AS_BLANK).parsedResult().stream()
            .map(MobileTransactionDetailsDto::toModel)
            .toList();
    List<MobileTransactionDetails> unsavedTransactions =
        getUnsavedTransactions(
            collectionUtils
                .filterDistinctByField(transactions, MobileTransactionDetails::getPspTransactionRef)
                .stream()
                .toList());

    mobilePaymentService.saveAll(unsavedTransactions);
    log.info("Verification done...");

    return transactions.stream()
        .map(MobileTransactionDetails::getPspTransactionRef)
        .collect(Collectors.toList());
  }

  private List<MobileTransactionDetails> getUnsavedTransactions(
      List<MobileTransactionDetails> transactions) {

    List<String> transactionRefs =
        transactions.stream().map(MobileTransactionDetails::getPspTransactionRef).toList();

    List<String> savedTransactionRefs =
        mobilePaymentService.findAllTransactionByRefs(transactionRefs).stream()
            .map(MobileTransactionDetails::getPspTransactionRef)
            .toList();

    return transactions.stream()
        .filter(t -> !savedTransactionRefs.contains(t.getPspTransactionRef()))
        .toList();
  }

  @Transactional
  public void checkMobilePaymentThenSaveVerification() {
    List<Mpbs> pendingMpbs = mpbsRepository.findAllByStatus(PENDING);
    log.info("pending mpbs = {}", pendingMpbs.size());
    verifyMobilePaymentAndSaveResult(pendingMpbs);
  }

  public List<TransactionDetails> fetchThenSaveTransactionDetailsDaily() {
    return mobilePaymentService.fetchTransactionDetails();
  }
}
