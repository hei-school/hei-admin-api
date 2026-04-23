package school.hei.haapi.service;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;

import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.MpbsMapper;
import school.hei.haapi.endpoint.rest.mapper.VolaMapper;
import school.hei.haapi.http.mapper.TransactionDetailsMapper;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.dto.MobileTransactionDetailsDto;
import school.hei.haapi.model.exception.NoRemainingAmountFee;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsVerification;
import school.hei.haapi.model.psp.vola.api.VolaClient;
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
  private final VolaClient volaClient;
  private final VolaMapper volaMapper;
  private final MpbsService mpbsService;
  private final MpbsMapper mapper;
  private final PaymentService paymentService;

  public List<MpbsVerification> findAllByStudentIdAndFeeId(String studentId, String feeId) {
    return repository.findAllByStudentIdAndFeeId(studentId, feeId);
  }

  public Mpbs verifyMpbsFromVola(Mpbs mpbs) {
    try {
      var studentEmail = mpbs.getStudent().getEmail();
      var volaPayment =
          volaClient.get(
              volaMapper.toPspType(mpbs.getMobileMoneyType()), mpbs.getPspId(), studentEmail);
      var verifiedMpbs =
          volaMapper.toMpbs(
              volaPayment, mpbs.getId(), mpbs.getStudent(), mpbs.getFee(), mpbs.getStatusHistory());
      if (verifiedMpbs.getAmount() == null) {
        return mpbs;
      }
      log.info(
          "Verifying Mpbs {} from Vola, result amount: {}", mpbs.getId(), verifiedMpbs.getAmount());
      paymentService.computeRemainingAmount(mpbs.getFee().getId(), verifiedMpbs.getAmount());
      return mpbsService.save(verifiedMpbs);
    } catch (Exception e) {
      log.error("Failed to verify Mpbs {} from Vola", mpbs.getId(), e);
      return mpbs;
    }
  }

  public void verifyPendingMpbsForStudent(String studentId) {
    var pendingMpbs = mpbsRepository.findAllByStatusAndStudentId(PENDING, studentId);
    pendingMpbs.forEach(this::verifyMpbsFromVola);
  }

  public school.hei.haapi.endpoint.rest.model.Mpbs sendVolaVerificationRequestAndSaveResult(
      Mpbs mpbs) {
    log.info("Creating Vola payment for mpbs {}", mpbs.getPspId());
    try {
      var volaPaymentResponse =
          volaClient.create(
              volaMapper.toPspType(mpbs.getMobileMoneyType()),
              mpbs.getPspId(),
              mpbs.getStudent().getEmail());
      log.info("Received Vola payment response for mpbs {}", mpbs.getPspId());
      var mpbsMappedFromVola =
          volaMapper.toMpbs(
              volaPaymentResponse,
              mpbs.getId(),
              mpbs.getStudent(),
              mpbs.getFee(),
              mpbs.getStatusHistory());

      return mapper.toRest(mpbsService.saveMpbs(mpbsMappedFromVola));
    } catch (Exception e) {
      log.error("Failed to create Vola payment for mpbs {}", mpbs.getPspId(), e);
      throw e;
    }
  }

  public List<MpbsVerification> verifyMobilePaymentAndSaveResult(List<Mpbs> pendingMpbsList) {
    List<MpbsVerification> verifiedMpbs = new ArrayList<>();
    List<Mpbs> unverifiedMpbs = new ArrayList<>();

    // Find all corresponding transaction in database
    List<MobileTransactionDetails> mobileTransactionResponseDetails =
        mobilePaymentService.findAllTransactionByMpbs(pendingMpbsList);

    // TIPS: do not use exception to continue script
    for (Mpbs pendingMbps : pendingMpbsList) {
      List<MobileTransactionDetails> correspondingTransactionsPendingDetails =
          mobileTransactionResponseDetails.stream()
              .filter(
                  transactionDetail ->
                      pendingMbps.getPspId().equals(transactionDetail.getPspTransactionRef()))
              .toList();
      if (correspondingTransactionsPendingDetails.size() > 1) {
        log.warn(
            "The payment has more than one transaction: {}",
            correspondingTransactionsPendingDetails);
      }
      Optional<MobileTransactionDetails> correspondingTransactionPendingDetails =
          correspondingTransactionsPendingDetails.stream()
              .max(
                  Comparator.comparing(
                      MobileTransactionDetails::getPspDatetimeTransactionCreation));

      if (!correspondingTransactionPendingDetails.isPresent()) {
        log.info(
            "no verification mobile payment details stored for the payment {}",
            pendingMbps.getId());
        unverifiedMpbs.add(pendingMbps);
        continue;
      }

      MobileTransactionDetails lastTransactionDetails =
          correspondingTransactionPendingDetails.get();
      if (!SUCCESS.equals(lastTransactionDetails.getStatus())) {
        log.info(
            "verification mobile payment details stored is not success for the payment {}",
            pendingMbps.getId());
        unverifiedMpbs.add(pendingMbps);
        continue;
      }

      try {
        TransactionDetails transactionDetails =
            transactionDetailsMapper.toExternalTransactionDetails(lastTransactionDetails);
        log.info("mapped transaction details = {}", transactionDetails);

        verifiedMpbs.add(
            computeVerifiedMobilePayment.saveTheVerifiedMpbs(pendingMbps, transactionDetails));
      } catch (NoRemainingAmountFee e) {
        log.error(
            "payment %s could not be verified because fee %s has no remaining amount"
                .formatted(pendingMbps.getId(), pendingMbps.getFee().getId()),
            e);
      } catch (RuntimeException e) {
        log.error(
            "Mpbs of ref {} could not be verified because of error", pendingMbps.getPspId(), e);
      }
    }

    unverifiedMobilePaymentHandler.accept(unverifiedMpbs);
    return verifiedMpbs;
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
