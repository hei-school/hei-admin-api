package school.hei.haapi.service;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static school.hei.haapi.service.utils.FileUtils.createFileFromBytes;
import static school.hei.haapi.service.utils.InstantUtils.getFirstDayOfActualMonth;

import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.LateFeeVerified;
import school.hei.haapi.endpoint.event.model.PojaEvent;
import school.hei.haapi.endpoint.event.model.StudentsWithOverdueFeesReminder;
import school.hei.haapi.endpoint.event.model.UnpaidFeesReminder;
import school.hei.haapi.endpoint.rest.model.AdvancedFeeStatisticsType;
import school.hei.haapi.endpoint.rest.model.FeeCategory;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.endpoint.rest.model.FeesStatistics;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.endpoint.rest.model.PaymentFrequency;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeTemplate;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.FeeDetailsDto;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.NoRemainingAmountFee;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.model.validator.UpdateFeeValidator;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.TransactionRepository;
import school.hei.haapi.repository.dao.FeeDao;
import school.hei.haapi.repository.model.FeesStats;
import school.hei.haapi.service.utils.XlsxCellsGenerator;

@Service
@AllArgsConstructor
@Slf4j
public class FeeService {
  private final FeeRepository feeRepository;
  private final FeeValidator feeValidator;
  private final UpdateFeeValidator updateFeeValidator;
  private final EventProducer<PojaEvent> eventProducer;
  private final FeeDao feeDao;
  private final CreditService creditService;
  private final FeeTemplateService feeTemplateService;
  private final FeeStatusHistoryService feeStatusHistoryService;
  private final BucketComponent bucketComponent;
  private final TransactionRepository transactionRepository;
  private static final String MONTHLY_FEE_TEMPLATE_NAME = "Frais mensuel L1";
  private static final String YEARLY_FEE_TEMPLATE_NAME = "Frais annuel L1";
  private static final List<String> HEADERS =
      List.of(
          "ref",
          "firstName",
          "lastName",
          "email",
          "totalAmount",
          "remainingAmount",
          "status",
          "category",
          "frequency",
          "comment",
          "creationDatetime",
          "dueDatetime",
          "addRefDate",
          "successfullyVerifiedAt");
  private final UserService userService;

  public byte[] generateFeesAsXlsx(FeeStatusEnum feeStatus, Instant from, Instant to) {
    XlsxCellsGenerator<Fee> xlsxCellsGenerator = new XlsxCellsGenerator<>();
    List<Fee> feeList = feeDao.findAllByStatusAndDueDatetimeBetween(feeStatus, from, to);
    return xlsxCellsGenerator.apply(
        feeList,
        List.of(
            "student.ref",
            "student.firstName",
            "student.lastName",
            "student.email",
            "totalAmount",
            "remainingAmount",
            "comment",
            "dueDatetime"));
  }

  public Fee debitAmountFromMpbs(Fee toUpdate, int amountToDebit) {
    int remainingAmount = toUpdate.getRemainingAmount();
    log.info("actual remaining amount before computing = {}", remainingAmount);
    if (remainingAmount == 0) {
      throw new NoRemainingAmountFee(toUpdate);
    }
    toUpdate.setRemainingAmount(remainingAmount - amountToDebit);

    int actualRemainingAmount = toUpdate.getRemainingAmount();
    log.info("actual remaining amount = {}", actualRemainingAmount);
    if (actualRemainingAmount <= 0) {
      log.info("if student paid over than expected = {}", actualRemainingAmount);
      toUpdate.setRemainingAmount(0);
      log.info(
          "set remaining amount even if student paid more = {}", toUpdate.getRemainingAmount());
    }
    return updateFeeStatus(toUpdate);
  }

  public Fee debitAmount(Fee toUpdate, int amountToDebit) {
    int remainingAmount = toUpdate.getRemainingAmount();

    if (remainingAmount == 0) {
      throw new NoRemainingAmountFee(toUpdate);
    }
    if (amountToDebit > remainingAmount) {
      throw new ApiException(SERVER_EXCEPTION, "Remaining amount is inferior to your request");
    }
    toUpdate.setRemainingAmount(remainingAmount - amountToDebit);
    return updateFeeStatus(toUpdate);
  }

  public Fee deleteFeeByStudentIdAndFeeId(String studentId, String feeId) {
    Fee deletedFee = getByStudentIdAndFeeId(studentId, feeId);
    feeRepository.deleteById(feeId);
    return deletedFee;
  }

  public Fee getById(String id) {
    var loggedFee =
        updateFeeStatus(
            feeRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Fee of id: " + id + " not found")));
    log.debug("fee: ------------########## {}", loggedFee);
    log.debug("now: ---------------######### {}", now());
    return loggedFee;
  }

  public Fee getByStudentIdAndFeeId(String studentId, String feeId) {
    return updateFeeStatus(feeRepository.getByStudentIdAndId(studentId, feeId));
  }

  @Transactional
  public List<Fee> saveAll(List<Fee> fees) {
    feeValidator.accept(fees);
    return feeRepository.saveAll(fees);
  }

  @Transactional
  public List<Fee> updateAll(List<Fee> fees) {
    updateFeeValidator.accept(fees);
    return feeRepository.saveAll(fees);
  }

  public Fee archiveFee(Fee fee) {
    updateFeeValidator.accept(fee);
    creditService.depositArchivedFee(fee);
    fee.setArchived(true);
    return feeRepository.save(fee);
  }

  public FeesStats getFeesStats(
      MpbsStatus mpbsStatus,
      FeeTypeEnum feeType,
      FeeStatusEnum status,
      FeeCategory feeCategory,
      Instant monthFrom,
      Instant monthTo,
      boolean isMpbs,
      String studentRef) {

    if (Objects.isNull(monthFrom)) monthFrom = getFirstDayOfActualMonth();

    var stats =
        feeDao.getStatByCriteria(
            mpbsStatus, feeType, status, feeCategory, studentRef, monthFrom, monthTo, isMpbs);
    return getHandledNullDataStats(stats);
  }

  public List<Fee> getFees(
      PageFromOne page,
      BoundedPageSize pageSize,
      MpbsStatus mpbsStatus,
      FeeTypeEnum feeType,
      FeeStatusEnum status,
      FeeCategory feeCategory,
      Instant monthFrom,
      Instant monthTo,
      boolean isMpbs,
      String studentRef) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());

    if (Objects.isNull(monthFrom)) monthFrom = getFirstDayOfActualMonth();

    return feeDao.getByCriteria(
        mpbsStatus, feeType, status, feeCategory, studentRef, monthFrom, monthTo, isMpbs, pageable);
  }

  public FeesStatistics getFeesStats(Instant monthFrom, Instant monthTo) {
    var result = feeDao.getStatByCriteria(null, null, null, null, null, monthFrom, monthTo, false);
    return FeesStats.to(getHandledNullDataStats(result));
  }

  private FeesStats getHandledNullDataStats(List<FeesStats> feesStats) {
    if (feesStats.isEmpty()) {
      return FeesStats.builder()
          .totalYearlyFees(0L)
          .totalMonthlyFees(0L)
          .totalFees(0L)
          .countOfPendingTransaction(0L)
          .countOfSuccessTransaction(0L)
          .totalLateFees(0L)
          .totalUnpaidFees(0L)
          .totalPaidFees(0L)
          .build();
    }
    return feesStats.getFirst();
  }

  /** The mpbs is sorted by creation date */
  public List<Fee> getFeesByStudentId(
      String studentId, PageFromOne page, BoundedPageSize pageSize, FeeStatusEnum status) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    if (status != null) {
      return feeRepository.getFeesByStudentIdAndStatusOrderByDueDatetimeDesc(
          studentId, status, pageable);
    }
    return feeRepository
        .findAllByStudentIdSortByStatusAndDueDatetimeDescAndId(studentId, pageable)
        .stream()
        .map(
            fee -> {
              fee.getMobilePayments().sort(Comparator.comparing(Mpbs::getCreationDatetime));
              return fee;
            })
        .toList();
  }

  private Fee updateFeeStatus(Fee initialFee) {
    if (initialFee.getRemainingAmount() == 0) {
      initialFee.updateStatus(PAID);
    } else if (initialFee.mustBeLate()) {
      initialFee.updateStatus(LATE);
    }
    feeStatusHistoryService.saveFeeStatus(initialFee.getStatus(), initialFee);
    return feeRepository.save(initialFee);
  }

  @Transactional
  public List<Fee> saveFromPaymentFrequency(
      User user, PaymentFrequency frequency, Instant firstDueDatetime) {

    List<Fee> feesToSave =
        switch (frequency) {
          case MONTHLY ->
              createFeesFromFeeTemplate(MONTHLY_FEE_TEMPLATE_NAME, user, firstDueDatetime);
          case YEARLY ->
              createFeesFromFeeTemplate(YEARLY_FEE_TEMPLATE_NAME, user, firstDueDatetime);
        };
    List<Fee> savedFees = feeRepository.saveAll(feesToSave);
    savedFees.forEach(fee -> feeStatusHistoryService.saveFeeStatus(fee.getStatus(), fee));
    return savedFees;
  }

  public List<Fee> createFeesFromFeeTemplate(String feeTemplateName, User user, Instant instant) {
    FeeTemplate feeTemplate = feeTemplateService.getFeeTemplateByName(feeTemplateName);
    List<Fee> fees = new ArrayList<>();
    for (int i = 0; i < feeTemplate.getNumberOfPayments(); i++) {
      Fee fee =
          Fee.builder()
              .id(randomUUID().toString())
              .comment(feeTemplate.getName())
              .totalAmount(feeTemplate.getAmount())
              .remainingAmount(feeTemplate.getAmount())
              .student(user)
              .creationDatetime(now())
              .status(UNPAID)
              .updatedAt(now())
              .dueDatetime(getDueDatetime(i, instant))
              .isDeleted(false)
              .type(TUITION)
              .category(feeTemplate.getCategory())
              .frequency(feeTemplate.getFrequency())
              .build();
      fees.add(fee);
    }
    return fees;
  }

  public Instant getDueDatetime(Integer monthToAdd, Instant instant) {
    return LocalDateTime.ofInstant(instant, ZoneId.of("UTC+3"))
        .plusMonths(monthToAdd)
        .atZone(ZoneId.of("UTC+3"))
        .toInstant();
  }

  @Transactional
  public List<Fee> updateFeesStatusToLate() {
    Instant now = now();
    List<Fee> unpaidFees = feeRepository.getUnpaidFees(now);
    var lateFees = new ArrayList<Fee>();
    unpaidFees.forEach(
        fee -> {
          var modifiedFee = updateFeeStatus(fee);
          log.info(
              "Fee "
                  + modifiedFee.describe()
                  + "with id."
                  + fee.getId()
                  + " is going to be updated from UNPAID to "
                  + fee.getStatus());
          if (LATE.equals(modifiedFee.getStatus())) {
            lateFees.add(modifiedFee);
          }
        });
    feeRepository.saveAll(lateFees);
    log.info("lateFees = {}", lateFees.stream().map(Fee::describe).toList());
    // Send list of late fees with student ref to contact
    if (!lateFees.isEmpty()) {
      eventProducer.accept(List.of(toStudentsWithOverdueFeesReminder(lateFees)));
    }
    return lateFees;
  }

  @Transactional
  public LateFeeVerified toLateFeeEvent(Fee fee) {
    return LateFeeVerified.builder()
        .type(fee.getType())
        .student(LateFeeVerified.FeeUser.from(fee.getStudent()))
        .comment(fee.getComment())
        .remainingAmount(fee.getRemainingAmount())
        .dueDatetime(fee.getDueDatetime())
        .build();
  }

  private UnpaidFeesReminder toUnpaidFeesReminder(Fee fee) {
    return UnpaidFeesReminder.builder()
        .user(UnpaidFeesReminder.UnpaidFeesUser.from(fee.getStudent()))
        .remainingAmount(fee.getRemainingAmount())
        .dueDatetime(fee.getDueDatetime())
        .build();
  }

  private static StudentsWithOverdueFeesReminder toStudentsWithOverdueFeesReminder(List<Fee> fees) {
    return StudentsWithOverdueFeesReminder.builder()
        .id(String.valueOf(randomUUID()))
        .students(
            fees.stream()
                .map(StudentsWithOverdueFeesReminder.StudentWithOverdueFees::from)
                .toList())
        .build();
  }

  @Transactional
  public void sendLateFeesEmail() {
    List<Fee> lateFees = feeRepository.findAllByStatus(LATE);
    log.info("Late fees size: " + lateFees.size());
    List<PojaEvent> lateFeeEvents =
        lateFees.stream()
            .map(
                lateFee -> {
                  log.info("Late Fee with id." + lateFee.getId() + " is sent to Queue");
                  return (PojaEvent) toLateFeeEvent(lateFee);
                })
            .toList();
    eventProducer.accept(lateFeeEvents);
  }

  public void sendUnpaidFeesEmail() {
    List<Fee> unpaidFees =
        feeRepository.getUnpaidFeesForTheMonthSpecified(
            now().atZone(ZoneId.of("UTC+3")).getMonthValue());
    log.info("Unpaid fees size: {}", unpaidFees.size());
    List<PojaEvent> unpaidFeeEvents =
        unpaidFees.stream()
            .map(
                unpaidFee -> {
                  log.info("Unpaid fee with id.{} is sent to Queue", unpaidFee.getId());
                  return (PojaEvent) toUnpaidFeesReminder(unpaidFee);
                })
            .toList();
    eventProducer.accept(unpaidFeeEvents);
  }

  public Fee pendFeeForMpbs(Fee fee) {
    fee.updateStatus(PENDING);
    feeStatusHistoryService.saveFeeStatus(fee.getStatus(), fee);
    return feeRepository.save(fee);
  }

  public String generateRawFees(Instant from, Instant to, AdvancedFeeStatisticsType type) {
    XlsxCellsGenerator<FeeDetailsDto> xlsxCellsGenerator = new XlsxCellsGenerator<>();
    var allFees =
        switch (type) {
          case ACCOUNTING -> feeRepository.findAllByDueDatetimeBetween(from, to);
          case RECEIPT -> feeRepository.findDistinctByStatusHistoriesDatetimeBetween(from, to);
        };
    var mappedFees = mapFees(allFees);
    var bytes = xlsxCellsGenerator.apply(mappedFees, HEADERS);
    var fileName = generateFileName(from, to);
    var file = createFileFromBytes(bytes, fileName, ".xlsx");
    var bucketKey = fileName + ".xlsx";
    bucketComponent.upload(file, bucketKey);
    return bucketComponent.presign(bucketKey, Duration.ofDays(1)).toString();
  }

  private List<FeeDetailsDto> mapFees(List<Fee> allFees) {
    return allFees.stream()
        .flatMap(
            fee -> {
              var allMpbs = fee.getMobilePayments();
              if (allMpbs.isEmpty() || allMpbs == null) {
                return Stream.of(FeeDetailsDto.from(fee, null));
              }
              return allMpbs.stream().map(mpb -> FeeDetailsDto.from(fee, mpb));
            })
        .toList();
  }

  private String generateFileName(Instant from, Instant to) {
    return "raw_fees_" + formatToDayMonthYear(from) + "_" + formatToDayMonthYear(to) + "_";
  }

  private static String formatToDayMonthYear(Instant instant) {
    return instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
  }
}
