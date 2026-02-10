package school.hei.haapi.service;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;

import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.http.mapper.TransactionDetailsMapper;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.PaymentVerificationResult;
import school.hei.haapi.model.dto.MobileTransactionDetailsDto;
import school.hei.haapi.model.exception.NoRemainingAmountFee;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsVerification;
import school.hei.haapi.model.psp.vola.api.VolaPsp;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PaymentId;
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
    log.info("Starting Vola verification for {} pending MPBS", pendingMpbsList.size());

    List<Mpbs> pendingMpbsCopy = new ArrayList<>(pendingMpbsList);

    Map<String, Payment> paymentMap;
    try {
      paymentMap = fetchAndMapVolaPayments(pendingMpbsCopy);
    } catch (Exception e) {
      log.error(
          "Fatal error fetching payments from Vola for {} MPBS - marking all as unverified",
          pendingMpbsCopy.size(),
          e);

      handleUnverifiedPayments(pendingMpbsCopy);
      logVerificationResults(List.of(), pendingMpbsCopy);
      return List.of();
    }

    var result = processPaymentsIndividually(pendingMpbsCopy, paymentMap);

    handleUnverifiedPayments(result.unverifiedMpbs());
    logVerificationResults(result.verifiedMpbs(), result.unverifiedMpbs());

    return result.verifiedMpbs();
  }

  private PaymentVerificationResult processPaymentsIndividually(
      List<Mpbs> pendingMpbsList, Map<String, Payment> paymentMap) {

    log.info("Processing {} MPBS against Vola payments", pendingMpbsList.size());

    List<MpbsVerification> verifiedMpbs = new ArrayList<>();
    List<Mpbs> unverifiedMpbs = new ArrayList<>();

    for (Mpbs pendingMpbs : pendingMpbsList) {
      try {
        processSinglePayment(pendingMpbs, paymentMap, verifiedMpbs, unverifiedMpbs);
      } catch (Exception e) {
        log.error(
            "Unexpected error processing payment with PSP ID: {} - marking as unverified",
            pendingMpbs.getPspId(),
            e);
        unverifiedMpbs.add(pendingMpbs);
      }
    }

    log.info(
        "Processed all payments - Verified: {}, Unverified: {}",
        verifiedMpbs.size(),
        unverifiedMpbs.size());

    return new PaymentVerificationResult(verifiedMpbs, unverifiedMpbs);
  }

  private void processSinglePayment(
      Mpbs pendingMpbs,
      Map<String, Payment> paymentMap,
      List<MpbsVerification> verifiedMpbs,
      List<Mpbs> unverifiedMpbs) {

    Payment volaPayment = paymentMap.get(pendingMpbs.getPspId());

    if (shouldVerifyPayment(volaPayment, pendingMpbs.getPspId())) {
      processVerifiedPayment(pendingMpbs, volaPayment, verifiedMpbs, unverifiedMpbs);
    } else {
      logUnverifiedReason(volaPayment, pendingMpbs.getPspId());
      unverifiedMpbs.add(pendingMpbs);
    }
  }

  private void processVerifiedPayment(
      Mpbs pendingMpbs,
      Payment volaPayment,
      List<MpbsVerification> verifiedMpbs,
      List<Mpbs> unverifiedMpbs) {

    try {
      var transactionDetails = transactionDetailsMapper.fromVolaPayment(volaPayment);
      log.info("Mapped transaction details from Vola = {}", transactionDetails);

      verifiedMpbs.add(
          computeVerifiedMobilePayment.saveTheVerifiedMpbs(pendingMpbs, transactionDetails));

    } catch (NoRemainingAmountFee e) {
      log.error(
          "Payment {} could not be verified because fee {} has no remaining amount",
          pendingMpbs.getId(),
          pendingMpbs.getFee().getId(),
          e);
      unverifiedMpbs.add(pendingMpbs);

    } catch (RuntimeException e) {
      log.error("Mpbs of ref {} could not be verified because of error", pendingMpbs.getPspId(), e);
      unverifiedMpbs.add(pendingMpbs);
    }
  }

  private Map<String, Payment> fetchAndMapVolaPayments(List<Mpbs> pendingMpbsList) {
    log.info("Fetching {} payments from Vola", pendingMpbsList.size());

    List<PaymentId> paymentIds = buildPaymentIds(pendingMpbsList);
    List<Payment> volaPayments = volaPsp.getPayments(paymentIds);

    log.info("Successfully fetched {} payments from Vola", volaPayments.size());

    return volaPayments.stream()
        .collect(Collectors.toMap(payment -> payment.getPspPayment().getId(), Function.identity()));
  }

  private List<PaymentId> buildPaymentIds(List<Mpbs> pendingMpbsList) {
    return pendingMpbsList.stream()
        .map(
            mpbs ->
                PaymentId.builder()
                    .payerEmail(mpbs.getStudent().getEmail())
                    .pspType(volaMapper.toPspTypeEnum(mpbs.getMobileMoneyType()))
                    .pspPaymentId(mpbs.getPspId())
                    .build())
        .toList();
  }

  private boolean shouldVerifyPayment(Payment volaPayment, String pspId) {
    return volaPayment != null && isPaymentSuccessful(volaPayment);
  }

  private boolean isPaymentSuccessful(Payment payment) {
    return payment.getVerificationStatus() == Payment.VerificationStatusEnum.SUCCEEDED;
  }

  private void logUnverifiedReason(Payment volaPayment, String pspId) {
    if (volaPayment == null) {
      log.info("No payment found in Vola for PSP ID: {}", pspId);
    } else if (volaPayment.getVerificationStatus() == Payment.VerificationStatusEnum.FAILED) {
      log.warn("Payment returned by Vola with FAILED status for PSP ID: {}", pspId);
    } else {
      log.info(
          "Payment from Vola is not successful for PSP ID: {} (status: {})",
          pspId,
          volaPayment.getVerificationStatus());
    }
  }

  private void handleUnverifiedPayments(List<Mpbs> unverifiedMpbs) {
    if (!unverifiedMpbs.isEmpty()) {
      log.info("Handling {} unverified payments", unverifiedMpbs.size());
      unverifiedMobilePaymentHandler.accept(unverifiedMpbs);
    }
  }

  private void logVerificationResults(
      List<MpbsVerification> verifiedMpbs, List<Mpbs> unverifiedMpbs) {
    log.info(
        "Vola verification completed - Verified: {}, Unverified: {}",
        verifiedMpbs.size(),
        unverifiedMpbs.size());
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
