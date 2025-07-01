package school.hei.haapi.service;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.service.utils.DateUtils.convertStringToInstant;

import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.http.mapper.ExternalResponseMapper;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsVerification;
import school.hei.haapi.model.Mpbs.TypedMobileMoneyTransaction;
import school.hei.haapi.model.exception.NoRemainingAmountFee;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.MpbsVerificationRepository;
import school.hei.haapi.service.aws.FileService;

@Service
@AllArgsConstructor
@Slf4j
public class MpbsVerificationService {
  private final MpbsVerificationRepository repository;
  private final MpbsRepository mpbsRepository;
  private final MobilePaymentService mobilePaymentService;
  private final ExternalResponseMapper externalResponseMapper;
  private final MultipartFileConverter multipartFileConverter;
  private final FileService fileService;
  private final MobilePaymentUnverifiedHandler mobilePaymentUnverifiedHandler;
  private final ComputeVerifiedMobilePayement computeVerifiedMobilePayement;

  public List<MpbsVerification> findAllByStudentIdAndFeeId(String studentId, String feeId) {
    return repository.findAllByStudentIdAndFeeId(studentId, feeId);
  }

  @Transactional
  public List<MpbsVerification> verifyMobilePaymentAndSaveResult(
      List<Mpbs> pendingMpbsList, Instant now) {
    log.info("Magic happened here");
    List<MpbsVerification> verifiedMpbs = new ArrayList<>();
    List<Mpbs> unverifiedMpbs = new ArrayList<>();

    // Find all corresponding transaction in database
    List<MobileTransactionDetails> mobileTransactionResponseDetails =
        mobilePaymentService.findAllTransactionByMpbsWithoutException(pendingMpbsList);

    // TIPS: do not use exception to continue script
    for (Mpbs pendingMbps : pendingMpbsList) {
      var correspondingTransactionPendingDetails =
          mobileTransactionResponseDetails.stream()
              .filter(
                  transactionDetail ->
                      pendingMbps.getPspId().equals(transactionDetail.getPspTransactionRef()))
              .max(
                  Comparator.comparing(
                      MobileTransactionDetails
                          ::getPspDatetimeTransactionCreation)); // Is it right ?

      if (correspondingTransactionPendingDetails.isPresent()) {
        try {
          MobileTransactionDetails firstCorrespondingTransactionDetails =
              correspondingTransactionPendingDetails.get();
          log.info("mobile transaction found = {}", firstCorrespondingTransactionDetails);
          TransactionDetails transactionDetails =
              externalResponseMapper.toExternalTransactionDetails(
                  firstCorrespondingTransactionDetails);
          log.info("mapped transaction details = {}", transactionDetails);

          verifiedMpbs.add(
              computeVerifiedMobilePayement.saveTheVerifiedMpbs(pendingMbps, transactionDetails));
        } catch (NoRemainingAmountFee e) {
          log.error("no remaining amount found", e);
        }
      } else {
        unverifiedMpbs.add(pendingMbps);
      }
    }

    mobilePaymentUnverifiedHandler.accept(unverifiedMpbs);

    return verifiedMpbs;
  }

  @Transactional
  public MpbsVerification verifyMobilePaymentAndSaveResult(Mpbs mpbs, Instant toCompare) {
    List<MpbsVerification> savedPayments =
        verifyMobilePaymentAndSaveResult(List.of(mpbs), toCompare);
    if (savedPayments.isEmpty()) {
      return null;
    }
    return savedPayments.getFirst();
  }

  @Transactional
  public List<Mpbs> computeFromXls(File file) throws IOException {
    List<String> pspToCheck = generateMobileTransactionDetailsFromXlsFile(file);

    List<Mpbs> mpbsToCheck = mpbsRepository.findByPspIdIn(pspToCheck);

    verifyMobilePaymentAndSaveResult(mpbsToCheck, Instant.now());
    return mpbsToCheck;
  }

  public String uploadXlsToS3(MultipartFile multipartFile) {
    String fileKey = "/XLS/" + multipartFile.getOriginalFilename();
    File file = multipartFileConverter.apply(multipartFile);
    fileService.uploadObjectToS3Bucket(fileKey, file);
    return fileKey;
  }

  public Workbook generateWorkBook(File file) throws IOException {
    try {
      return new HSSFWorkbook(new FileInputStream(file));
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  private List<String> generateMobileTransactionDetailsFromXlsFile(File file) throws IOException {
    log.info("Reading XLS file...");
    List<String> pendingMpbsPspIds =
        mpbsRepository.findAllByStatus(PENDING).stream()
            .map(TypedMobileMoneyTransaction::getPspId)
            .toList();

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

      if (pendingMpbsPspIds.contains(ref)) {

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
    mobilePaymentService.saveAll(transactions);
    log.info("Verification done...");
    return transactions.stream()
        .map(MobileTransactionDetails::getPspTransactionRef)
        .collect(Collectors.toList());
  }

  @Transactional
  public void checkMobilePaymentThenSaveVerification() {
    List<Mpbs> pendingMpbs = mpbsRepository.findAllByStatus(PENDING);
    log.info("pending mpbs = {}", pendingMpbs.size());
    Instant now = Instant.now();

    verifyMobilePaymentAndSaveResult(pendingMpbs, now);
  }

  public List<TransactionDetails> fetchThenSaveTransactionDetailsDaily() {
    return mobilePaymentService.fetchThenSaveTransactionDetails();
  }
}
