package school.hei.haapi.service;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;
import static school.hei.haapi.service.utils.DateUtils.convertStringToInstant;

import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.http.mapper.TransactionDetailsMapper;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.exception.NoRemainingAmountFee;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsVerification;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.MpbsVerificationRepository;
import school.hei.haapi.service.aws.FileService;
import school.hei.haapi.service.utils.CollectionUtils;

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

  public List<MpbsVerification> findAllByStudentIdAndFeeId(String studentId, String feeId) {
    return repository.findAllByStudentIdAndFeeId(studentId, feeId);
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
    generateMobileTransactionDetailsFromXlsFile(file);

    List<Mpbs> mpbsToVerify = mpbsRepository.findAllByStatus(PENDING);

    verifyMobilePaymentAndSaveResult(mpbsToVerify);
    return mpbsToVerify;
  }

  public String uploadXlsToS3(MultipartFile multipartFile) {
    String fileKey = "/XLS/" + multipartFile.getOriginalFilename();
    File file = multipartFileConverter.apply(multipartFile);
    fileService.uploadObjectToS3Bucket(fileKey, file);
    return fileKey;
  }

  public Workbook generateWorkBook(File file) throws IOException {
    try {
      return WorkbookFactory.create(file);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  private List<String> generateMobileTransactionDetailsFromXlsFile(File file) throws IOException {
    log.info("Reading XLS file...");

    List<MobileTransactionDetails> transactions = new ArrayList<>();

    Workbook workbook = generateWorkBook(file);

    Sheet sheet = workbook.getSheetAt(0);

    for (Row row : sheet) {

      Cell dateCell = row.getCell(1);
      Cell timeCell = row.getCell(2);
      Cell refCell = row.getCell(3);
      Cell statusCell = row.getCell(6);
      Cell montantCell = row.getCell(14);

      if (dateCell == null
          || timeCell == null
          || StringUtils.isBlank(dateCell.getStringCellValue())
          || StringUtils.isBlank(timeCell.getStringCellValue())) {
        log.warn("Row {} ignored because of an empty cell", row.getRowNum());
        continue;
      }

      String dateTimeStr =
          dateCell.getStringCellValue().trim() + " " + timeCell.getStringCellValue().trim();
      String ref = refCell.getStringCellValue().trim();

      if (ref.matches("^MP.{18}$")) {
        Instant transactionCreationTime;
        try {
          transactionCreationTime = Instant.from(convertStringToInstant(dateTimeStr));
        } catch (Exception e) {
          log.warn("Failed to parse date/time for row {}: {}", row.getRowNum(), e.getMessage());
          continue;
        }

        MobileTransactionDetails transaction =
            MobileTransactionDetails.builder()
                .id(randomUUID().toString())
                .pspDatetimeTransactionCreation(transactionCreationTime)
                .pspTransactionRef(refCell.getStringCellValue().trim())
                .pspTransactionAmount((int) montantCell.getNumericCellValue())
                .status(
                    MpbsStatus.fromValue(
                        Objects.equals(statusCell.getStringCellValue().trim(), "Succès")
                            ? "SUCCESS"
                            : "FAILED"))
                .pspOwnDatetimeVerification(Instant.now())
                .build();

        transactions.add(transaction);
        log.info("Generated mobile transaction psp id {}", transaction.getPspTransactionRef());
      } else {
        log.info("Unverified mobile transaction psp id {}", ref);
      }
    }
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
