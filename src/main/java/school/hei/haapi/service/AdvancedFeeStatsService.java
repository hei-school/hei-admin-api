package school.hei.haapi.service;

import static java.time.Instant.now;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.groupingByConcurrent;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L2;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L3;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.WORK_FEES;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.YEARLY;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.REMEDIAL_COSTS;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static school.hei.haapi.model.fee.PaymentType.BANK;
import static school.hei.haapi.model.fee.PaymentType.MPBS;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType.ACCOUNTING;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.LATE_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PAID_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PENDING_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.TOTAL_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.UNPAID_COUNT;
import static school.hei.haapi.service.utils.FileUtils.createFileFromBytes;

import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.AdvancedFeeStatsComputationTriggered;
import school.hei.haapi.endpoint.rest.mapper.AdvancedFeeStatsMapper;
import school.hei.haapi.endpoint.rest.model.AdvancedFeeStatisticsType;
import school.hei.haapi.endpoint.rest.model.AdvancedFeesStatistics;
import school.hei.haapi.endpoint.rest.model.FeeCategory;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.endpoint.rest.model.LateFeesStats;
import school.hei.haapi.endpoint.rest.model.PaidFeesStats;
import school.hei.haapi.endpoint.rest.model.PendingFeesStats;
import school.hei.haapi.endpoint.rest.model.TotalExpectedFeesStats;
import school.hei.haapi.endpoint.rest.model.UnpaidFeesStats;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.fee.PaymentType;
import school.hei.haapi.model.statistics.AdvancedFeeStats;
import school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType;
import school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType;
import school.hei.haapi.repository.AdvancedFeeStatsRepository;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.dao.FeeDao;
import school.hei.haapi.service.utils.DateUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedFeeStatsService {
  private final FeeDao feeDao;
  private final AdvancedFeeStatsRepository repository;
  private final AdvancedFeeStatsMapper advancedFeeStatsMapper;
  private final FeeRepository feeRepository;
  private final EventProducer<AdvancedFeeStatsComputationTriggered> eventProducer;
  private final BucketComponent bucketComponent;

  private static final Duration ADVANCED_FEE_STATS_EXPIRATION = Duration.of(3, DAYS);
  private static final List<String> STATS_HEADERS =
      List.of(
          "Date de création",
          "L1",
          "L2",
          "L3",
          "Niveau non défini",
          "Alternants",
          "Mensuel",
          "Annuel",
          "Fréquence non défini",
          "Virement bancaire",
          "Orange money",
          "Date de modification",
          "Type de stats");

  @Transactional
  public AdvancedFeesStatistics getAdvancedFeeStats(
      LocalDate dateFrom, LocalDate dateTo, Optional<AdvancedFeeStatsCountType> type) {
    if (type.isEmpty()) {
      log.warn(
          "No count type provided for advanced stats query. ACCOUNTING stats will be returned by"
              + " default");
    }
    LocalDate now = LocalDate.now();
    LocalDate from = Optional.ofNullable(dateFrom).orElse(now.withDayOfMonth(1));
    LocalDate to = Optional.ofNullable(dateTo).orElse(now.with(lastDayOfMonth()));
    var advancedStats = feeDao.getAdvancedFeeStatsOnDateBetween(from, to, type.orElse(ACCOUNTING));
    if (advancedStats.isEmpty()) {
      log.info("Advanced stats is empty. Now generating {} stats for... {} {}", type, from, to);
      eventProducer.accept(
          List.of(
              new AdvancedFeeStatsComputationTriggered(
                  from.atStartOfDay(), to.atTime(23, 59, 59), type)));
      return new AdvancedFeesStatistics().expired(true);
    }

    if (shouldBeUpdated(advancedStats)) {
      log.info("Advanced stats is expired. Now updating {} stats for... {} {}", type, from, to);
      eventProducer.accept(
          List.of(
              new AdvancedFeeStatsComputationTriggered(
                  from.atStartOfDay(), to.atTime(23, 59, 59), type)));
      return advancedFeeStatsMapper.toRest(advancedStats, true);
    }

    return advancedFeeStatsMapper.toRest(advancedStats, false);
  }

  private boolean shouldBeUpdated(Map<AdvancedFeeStatsType, AdvancedFeeStats> advancedStats) {
    return advancedStats.values().stream()
        .anyMatch(e -> e.getUpdateDatetime().isBefore(now().minus(ADVANCED_FEE_STATS_EXPIRATION)));
  }

  public String generateAdvancedFeesStatsExcelFile(
      Instant from,
      Instant to,
      FeeStatusEnum feeStatsEnum,
      AdvancedFeeStatisticsType statisticsType) {
    AdvancedFeeStatsCountType countType = AdvancedFeeStatsCountType.valueOf(statisticsType.name());
    var advancedFeesStats =
        generateAdvancedFeeStats(Optional.ofNullable(from), Optional.ofNullable(to), countType);
    if (feeStatsEnum != null) {
      var feeStatsType = getFeesStatsType(feeStatsEnum);
      advancedFeesStats =
          advancedFeesStats.stream()
              .filter(advancedFeeStats -> feeStatsType.equals(advancedFeeStats.getStatType()))
              .toList();
    }
    try (var workbook = new XSSFWorkbook();
        var bytes = new ByteArrayOutputStream()) {
      var sheet = workbook.createSheet("STATS");
      var headerToUse = sheet.createRow(0);
      var cellIndex = 0;
      for (var header : STATS_HEADERS) {
        headerToUse.createCell(cellIndex++).setCellValue(header);
      }
      var createHelper = workbook.getCreationHelper();
      var dateCellStyle = workbook.createCellStyle();
      dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm"));
      fillRows(advancedFeesStats, sheet, dateCellStyle);

      for (int i = 0; i < STATS_HEADERS.size(); i++) {
        sheet.autoSizeColumn(i);
      }
      workbook.write(bytes);
      var now = Instant.now().truncatedTo(SECONDS);
      var file = createFileFromBytes(bytes.toByteArray(), "advanced-fees-stats-" + now, ".xlsx");
      var bucketKey = "advanced-fees-stats-" + now + ".xlsx";
      bucketComponent.upload(file, bucketKey);
      return bucketComponent.presign(bucketKey, Duration.ofDays(1)).toString();
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  private static AdvancedFeeStatsType getFeesStatsType(FeeStatusEnum type) {
    var advanceFeesStatsType =
        switch (type) {
          case UNPAID -> UNPAID_COUNT;
          case LATE -> LATE_COUNT;
          case PENDING -> PENDING_COUNT;
          case PAID -> PAID_COUNT;
        };
    return advanceFeesStatsType;
  }

  private static void fillRows(
      List<AdvancedFeeStats> filteredAdvancedStats, Sheet sheet, CellStyle dateCellStyle) {
    int rowIndex = 1;
    for (var stat : filteredAdvancedStats) {
      var row = sheet.createRow(rowIndex++);

      var creationCell = row.createCell(0);
      Optional.ofNullable(stat.getCreationDatetime())
          .ifPresent(
              date -> {
                creationCell.setCellValue(Date.from(date));
                creationCell.setCellStyle(dateCellStyle);
              });

      row.createCell(1)
          .setCellValue(Optional.ofNullable(stat.getFirstGradeCountMonthly()).orElse(0L));
      row.createCell(2)
          .setCellValue(Optional.ofNullable(stat.getSecondGradeCountMonthly()).orElse(0L));
      row.createCell(3)
          .setCellValue(Optional.ofNullable(stat.getThirdGradeCountMonthly()).orElse(0L));
      row.createCell(4).setCellValue(Optional.ofNullable(stat.getUnknownGradeCount()).orElse(0L));
      row.createCell(5).setCellValue(Optional.ofNullable(stat.getWorkStudyCount()).orElse(0L));
      row.createCell(6).setCellValue(Optional.ofNullable(stat.getMonthlyCount()).orElse(0L));
      row.createCell(7).setCellValue(Optional.ofNullable(stat.getYearlyCount()).orElse(0L));
      row.createCell(8)
          .setCellValue(Optional.ofNullable(stat.getUnknownFrequencyCount()).orElse(0L));
      row.createCell(9).setCellValue(Optional.ofNullable(stat.getBankTransferCount()).orElse(0L));
      row.createCell(10).setCellValue(Optional.ofNullable(stat.getMpbsCount()).orElse(0L));

      var updateCell = row.createCell(11);
      Optional.ofNullable(stat.getUpdateDatetime())
          .ifPresent(
              d -> {
                updateCell.setCellValue(Date.from(d));
                updateCell.setCellStyle(dateCellStyle);
              });

      var typeCell = row.createCell(12);
      Optional.ofNullable(stat.getStatType()).ifPresent(type -> typeCell.setCellValue(type.name()));
    }
  }

  public List<AdvancedFeeStats> generateAdvancedFeeStats(
      Optional<Instant> fromInstant, Optional<Instant> toInstant, AdvancedFeeStatsCountType type) {
    LocalDate now = LocalDate.now();
    Optional<LocalDate> fromDate = fromInstant.map(instant -> instant.atZone(UTC).toLocalDate());
    Optional<LocalDate> toDate = toInstant.map(instant -> instant.atZone(UTC).toLocalDate());

    DateUtils.TimeRange<LocalDate> dateRange =
        new DateUtils.TimeRange<>(
            fromDate.orElse(now.withDayOfMonth(1)), toDate.orElse(now.with(lastDayOfMonth())));

    return new ArrayList<>(
        generateAdvancedFeeStatsOnDateBetween(dateRange.from(), dateRange.to(), type));
  }

  private Collection<AdvancedFeeStats> generateAdvancedFeeStatsOnDateBetween(
      LocalDate fromDate, LocalDate toDate, AdvancedFeeStatsCountType type) {
    Instant dayStart = fromDate.atStartOfDay().toInstant(UTC);
    Instant dayEnd = toDate.atTime(23, 59, 59).toInstant(UTC);

    List<Fee> allFees =
        switch (type) {
          case ACCOUNTING -> feeRepository.findAllByDueDatetimeBetween(dayStart, dayEnd);
          case RECEIPT ->
              feeRepository.findDistinctByStatusHistoriesDatetimeBetween(dayStart, dayEnd);
        };

    Collection<AdvancedFeeStats> stats =
        feeDao.getAdvancedFeeStatsOnDateBetween(fromDate, toDate, type).values();
    Optional<LocalDate> date = statCountDateMapper(type, toDate);

    AdvancedFeesStatistics restStats = generateAdvancedFeeStats(allFees, date);
    if (!stats.isEmpty()) {
      stats.forEach(
          stat -> {
            switch (stat.getStatType()) {
              case PENDING_COUNT -> handlePendingFeesCount(stat, restStats);
              case LATE_COUNT -> handleLateFeesCount(stat, restStats);
              case PAID_COUNT -> handlePaidFeesCount(stat, restStats);
              case UNPAID_COUNT -> handleUnpaidFeesCount(stat, restStats);
              case TOTAL_COUNT -> handleTotalFeesCount(stat, restStats);
            }
          });
      return stats;
    }

    return advancedFeeStatsMapper.fromRest(restStats, fromDate, toDate, type).values();
  }

  private Optional<LocalDate> statCountDateMapper(AdvancedFeeStatsCountType type, LocalDate date) {
    return switch (type) {
      case ACCOUNTING -> Optional.empty();
      case RECEIPT -> Optional.of(date);
    };
  }

  private AdvancedFeesStatistics generateAdvancedFeeStats(
      List<Fee> fees, Optional<LocalDate> statusDate) {
    return new AdvancedFeesStatistics()
        .lateFeesCount(getLateFeesStats(fees, statusDate))
        .paidFeesCount(getPaidFeesStats(fees, statusDate))
        .pendingFeesCount(getPendingFeesStats(fees, statusDate))
        .unpaidFeesCount(getUnpaidFeeStats(fees, statusDate))
        .totalExpectedFeesCount(getTotalExpectedFeesStats(fees));
  }

  private void handlePendingFeesCount(
      AdvancedFeeStats feeStats, AdvancedFeesStatistics advancedFeesStatistics) {
    var pendingFeesStats = advancedFeesStatistics.getPendingFeesCount();
    handleFeesCount(
        feeStats,
        pendingFeesStats.getFirstGradeMonthly(),
        pendingFeesStats.getSecondGradeMonthly(),
        pendingFeesStats.getThirdGradeMonthly(),
        pendingFeesStats.getFirstGradeYearly(),
        pendingFeesStats.getSecondGradeYearly(),
        pendingFeesStats.getThirdGradeYearly(),
        pendingFeesStats.getUnknownGrade(),
        pendingFeesStats.getWorkStudy(),
        pendingFeesStats.getRetakeExamFirstGradeCount(),
        pendingFeesStats.getRetakeExamSecondGradeCount(),
        pendingFeesStats.getRetakeExamThirdGradeCount(),
        pendingFeesStats.getMonthly(),
        pendingFeesStats.getYearly(),
        pendingFeesStats.getUnknownFrequency());
  }

  void handleUnpaidFeesCount(
      AdvancedFeeStats feeStats, AdvancedFeesStatistics advancedFeesStatistics) {
    var unpaidFeesStats = advancedFeesStatistics.getUnpaidFeesCount();
    handleFeesCount(
        feeStats,
        unpaidFeesStats.getFirstGradeMonthly(),
        unpaidFeesStats.getSecondGradeMonthly(),
        unpaidFeesStats.getThirdGradeMonthly(),
        unpaidFeesStats.getFirstGradeYearly(),
        unpaidFeesStats.getSecondGradeYearly(),
        unpaidFeesStats.getThirdGradeYearly(),
        unpaidFeesStats.getUnknownGrade(),
        unpaidFeesStats.getWorkStudy(),
        unpaidFeesStats.getRetakeExamFirstGradeCount(),
        unpaidFeesStats.getRetakeExamSecondGradeCount(),
        unpaidFeesStats.getRetakeExamThirdGradeCount(),
        unpaidFeesStats.getMonthly(),
        unpaidFeesStats.getYearly(),
        unpaidFeesStats.getUnknownFrequency());
  }

  private void handleLateFeesCount(
      AdvancedFeeStats feeStats, AdvancedFeesStatistics advancedFeesStatistics) {
    var lateFeesStats = advancedFeesStatistics.getLateFeesCount();
    handleFeesCount(
        feeStats,
        lateFeesStats.getFirstGradeMonthly(),
        lateFeesStats.getSecondGradeMonthly(),
        lateFeesStats.getThirdGradeMonthly(),
        lateFeesStats.getFirstGradeYearly(),
        lateFeesStats.getSecondGradeYearly(),
        lateFeesStats.getThirdGradeYearly(),
        lateFeesStats.getUnknownGrade(),
        lateFeesStats.getWorkStudy(),
        lateFeesStats.getRetakeExamFirstGradeCount(),
        lateFeesStats.getRetakeExamSecondGradeCount(),
        lateFeesStats.getRetakeExamThirdGradeCount(),
        lateFeesStats.getMonthly(),
        lateFeesStats.getYearly(),
        lateFeesStats.getUnknownFrequency());
  }

  private void handlePaidFeesCount(
      AdvancedFeeStats feeStats, AdvancedFeesStatistics advancedFeesStatistics) {
    var paidFeesStats = advancedFeesStatistics.getPaidFeesCount();
    handleFeesCount(
        feeStats,
        paidFeesStats.getFirstGradeMonthly(),
        paidFeesStats.getSecondGradeMonthly(),
        paidFeesStats.getThirdGradeMonthly(),
        paidFeesStats.getFirstGradeYearly(),
        paidFeesStats.getSecondGradeYearly(),
        paidFeesStats.getThirdGradeYearly(),
        paidFeesStats.getUnknownGrade(),
        paidFeesStats.getWorkStudy(),
        paidFeesStats.getRetakeExamFirstGradeCount(),
        paidFeesStats.getRetakeExamSecondGradeCount(),
        paidFeesStats.getRetakeExamThirdGradeCount(),
        paidFeesStats.getMonthly(),
        paidFeesStats.getYearly(),
        paidFeesStats.getUnknownFrequency());
    feeStats.setBankTransferCount(paidFeesStats.getBankFees().longValue());
    feeStats.setMpbsCount(paidFeesStats.getMobileMoney().longValue());
  }

  private void handleTotalFeesCount(
      AdvancedFeeStats feeStats, AdvancedFeesStatistics advancedFeesStatistics) {
    var totalExpectedFeesStats = advancedFeesStatistics.getTotalExpectedFeesCount();
    handleFeesCount(
        feeStats,
        totalExpectedFeesStats.getFirstGradeMonthly(),
        totalExpectedFeesStats.getSecondGradeMonthly(),
        totalExpectedFeesStats.getThirdGradeMonthly(),
        totalExpectedFeesStats.getFirstGradeYearly(),
        totalExpectedFeesStats.getSecondGradeYearly(),
        totalExpectedFeesStats.getThirdGradeYearly(),
        totalExpectedFeesStats.getUnknownGrade(),
        totalExpectedFeesStats.getWorkStudy(),
        totalExpectedFeesStats.getRetakeExamFirstGradeCount(),
        totalExpectedFeesStats.getRetakeExamSecondGradeCount(),
        totalExpectedFeesStats.getRetakeExamThirdGradeCount(),
        totalExpectedFeesStats.getMonthly(),
        totalExpectedFeesStats.getYearly(),
        totalExpectedFeesStats.getUnknownFrequency());
  }

  private void handleFeesCount(
      AdvancedFeeStats feeStats,
      Long firstGradeMonthly,
      Long secondGradeMonthly,
      Long thirdGradeMonthly,
      Long firstGradeYearly,
      Long secondGradeYearly,
      Long thirdGradeYearly,
      Long unknownGrade,
      Long workStudy,
      Long retakeExamFirstGradeCount,
      Long retakeExamSecondGradeCount,
      Long retakeExamThirdGradeCount,
      Long monthly,
      Long yearly,
      Long unknownFrequency) {
    feeStats.setFirstGradeCountMonthly(firstGradeMonthly);
    feeStats.setSecondGradeCountMonthly(secondGradeMonthly);
    feeStats.setThirdGradeCountMonthly(thirdGradeMonthly);
    feeStats.setFirstGradeCountYearly(firstGradeYearly);
    feeStats.setSecondGradeCountYearly(secondGradeYearly);
    feeStats.setThirdGradeCountYearly(thirdGradeYearly);
    feeStats.setUnknownGradeCount(unknownGrade);
    feeStats.setWorkStudyCount(workStudy);
    feeStats.setRemedialFirstGradeCount(retakeExamFirstGradeCount);
    feeStats.setRemedialSecondGradeCount(retakeExamSecondGradeCount);
    feeStats.setRemedialThirdGradeCount(retakeExamThirdGradeCount);
    feeStats.setMonthlyCount(monthly);
    feeStats.setYearlyCount(yearly);
    feeStats.setUpdateDatetime(now());
    feeStats.setUnknownFrequencyCount(unknownFrequency);
  }

  private LateFeesStats getLateFeesStats(List<Fee> fees, Optional<LocalDate> statusDate) {
    var lateFees = filterFeesByStatus(fees, LATE, statusDate);
    Map<FeeCategory, Long> monthlyFeeCountByCategory = countFeesByGrades(lateFees, MONTHLY);
    Map<FeeCategory, Long> yearlyFeeCountByCategory = countFeesByGrades(lateFees, YEARLY);
    var unknownGradeFees =
        monthlyFeeCountByCategory.get(FeeCategory.UNKNOWN)
            + yearlyFeeCountByCategory.get(FeeCategory.UNKNOWN);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(lateFees);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<FeeFrequency, Long> feesCountByPaymentFrequency = countFeesByPaymentFrequency(tuitionFees);
    return new LateFeesStats()
        .retakeExamFirstGradeCount(countRemedialFees(lateFees, L1))
        .retakeExamSecondGradeCount(countRemedialFees(lateFees, L2))
        .retakeExamThirdGradeCount(countRemedialFees(lateFees, L3))
        .workStudy(monthlyFeeCountByCategory.get(WORK_FEES))
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .unknownFrequency(feesCountByPaymentFrequency.get(UNKNOWN))
        .firstGradeMonthly(monthlyFeeCountByCategory.get(L1))
        .secondGradeMonthly(monthlyFeeCountByCategory.get(L2))
        .thirdGradeMonthly(monthlyFeeCountByCategory.get(L3))
        .firstGradeYearly(yearlyFeeCountByCategory.get(L1))
        .secondGradeYearly(yearlyFeeCountByCategory.get(L2))
        .thirdGradeYearly(yearlyFeeCountByCategory.get(L3))
        .unknownGrade(unknownGradeFees);
  }

  private PaidFeesStats getPaidFeesStats(List<Fee> fees, Optional<LocalDate> statusDate) {
    var paidFees = filterFeesByStatus(fees, PAID, statusDate);
    Map<FeeCategory, Long> monthlyFeeCountByCategory = countFeesByGrades(paidFees, MONTHLY);
    Map<FeeCategory, Long> yearlyFeeCountByCategory = countFeesByGrades(paidFees, YEARLY);
    var unknownGradeFees =
        monthlyFeeCountByCategory.get(FeeCategory.UNKNOWN)
            + yearlyFeeCountByCategory.get(FeeCategory.UNKNOWN);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(paidFees);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<FeeFrequency, Long> feesCountByPaymentFrequency = countFeesByPaymentFrequency(tuitionFees);
    Map<PaymentType, Long> feesCountByPaymentType = countFeesByPaymentType(tuitionFees);
    return new PaidFeesStats()
        .retakeExamFirstGradeCount(countRemedialFees(paidFees, L1))
        .retakeExamSecondGradeCount(countRemedialFees(paidFees, L2))
        .retakeExamThirdGradeCount(countRemedialFees(paidFees, L3))
        .unknownFrequency(feesCountByPaymentFrequency.get(UNKNOWN))
        .firstGradeMonthly(monthlyFeeCountByCategory.get(L1))
        .secondGradeMonthly(monthlyFeeCountByCategory.get(L2))
        .thirdGradeMonthly(monthlyFeeCountByCategory.get(L3))
        .firstGradeYearly(yearlyFeeCountByCategory.get(L1))
        .secondGradeYearly(yearlyFeeCountByCategory.get(L2))
        .thirdGradeYearly(yearlyFeeCountByCategory.get(L3))
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .workStudy(monthlyFeeCountByCategory.get(WORK_FEES))
        .unknownGrade(unknownGradeFees)
        .bankFees(BigDecimal.valueOf(feesCountByPaymentType.get(BANK)))
        .mobileMoney(BigDecimal.valueOf(feesCountByPaymentType.get(MPBS)));
  }

  private PendingFeesStats getPendingFeesStats(List<Fee> fees, Optional<LocalDate> statusDate) {
    var pendingFees = filterFeesByStatus(fees, PENDING, statusDate);
    Map<FeeCategory, Long> monthlyFeeCountByCategory = countFeesByGrades(pendingFees, MONTHLY);
    Map<FeeCategory, Long> yearlyFeeCountByCategory = countFeesByGrades(pendingFees, YEARLY);
    var unknownGradeFees =
        monthlyFeeCountByCategory.get(FeeCategory.UNKNOWN)
            + yearlyFeeCountByCategory.get(FeeCategory.UNKNOWN);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(pendingFees);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<FeeFrequency, Long> feesCountByPaymentFrequency = countFeesByPaymentFrequency(tuitionFees);
    return new PendingFeesStats()
        .retakeExamFirstGradeCount(countRemedialFees(pendingFees, L1))
        .retakeExamSecondGradeCount(countRemedialFees(pendingFees, L2))
        .retakeExamThirdGradeCount(countRemedialFees(pendingFees, L3))
        .workStudy(monthlyFeeCountByCategory.get(WORK_FEES))
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .unknownFrequency(feesCountByPaymentFrequency.get(UNKNOWN))
        .firstGradeMonthly(monthlyFeeCountByCategory.get(L1))
        .secondGradeMonthly(monthlyFeeCountByCategory.get(L2))
        .thirdGradeMonthly(monthlyFeeCountByCategory.get(L3))
        .firstGradeYearly(yearlyFeeCountByCategory.get(L1))
        .secondGradeYearly(yearlyFeeCountByCategory.get(L2))
        .thirdGradeYearly(yearlyFeeCountByCategory.get(L3))
        .unknownGrade(unknownGradeFees);
  }

  private TotalExpectedFeesStats getTotalExpectedFeesStats(List<Fee> fees) {
    Map<FeeCategory, Long> monthlyFeeCountByCategory = countFeesByGrades(fees, MONTHLY);
    Map<FeeCategory, Long> yearlyFeeCountByCategory = countFeesByGrades(fees, YEARLY);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(fees);
    var unknownGradeFees =
        monthlyFeeCountByCategory.get(FeeCategory.UNKNOWN)
            + yearlyFeeCountByCategory.get(FeeCategory.UNKNOWN);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<FeeFrequency, Long> feesCountByPaymentFrequency = countFeesByPaymentFrequency(tuitionFees);
    return new TotalExpectedFeesStats()
        .retakeExamFirstGradeCount(countRemedialFees(fees, L1))
        .retakeExamSecondGradeCount(countRemedialFees(fees, L2))
        .retakeExamThirdGradeCount(countRemedialFees(fees, L3))
        .unknownFrequency(feesCountByPaymentFrequency.get(UNKNOWN))
        .firstGradeMonthly(monthlyFeeCountByCategory.get(L1))
        .secondGradeMonthly(monthlyFeeCountByCategory.get(L2))
        .thirdGradeMonthly(monthlyFeeCountByCategory.get(L3))
        .firstGradeYearly(yearlyFeeCountByCategory.get(L1))
        .secondGradeYearly(yearlyFeeCountByCategory.get(L2))
        .thirdGradeYearly(yearlyFeeCountByCategory.get(L3))
        .unknownGrade(unknownGradeFees)
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .workStudy(monthlyFeeCountByCategory.get(WORK_FEES));
  }

  private UnpaidFeesStats getUnpaidFeeStats(List<Fee> fees, Optional<LocalDate> statusDate) {
    var unpaidFees = filterFeesByStatus(fees, UNPAID, statusDate);
    Map<FeeCategory, Long> monthlyFeeCountByCategory = countFeesByGrades(unpaidFees, MONTHLY);
    Map<FeeCategory, Long> yearlyFeeCountByCategory = countFeesByGrades(unpaidFees, YEARLY);
    var unknownGradeFees =
        monthlyFeeCountByCategory.get(FeeCategory.UNKNOWN)
            + yearlyFeeCountByCategory.get(FeeCategory.UNKNOWN);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(unpaidFees);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<FeeFrequency, Long> feesCountByPaymentFrequency = countFeesByPaymentFrequency(tuitionFees);
    return new UnpaidFeesStats()
        .retakeExamFirstGradeCount(countRemedialFees(fees, L1))
        .retakeExamSecondGradeCount(countRemedialFees(fees, L2))
        .retakeExamThirdGradeCount(countRemedialFees(fees, L3))
        .workStudy(monthlyFeeCountByCategory.get(WORK_FEES))
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .unknownFrequency(feesCountByPaymentFrequency.get(UNKNOWN))
        .unknownFrequency(feesCountByPaymentFrequency.get(UNKNOWN))
        .firstGradeMonthly(monthlyFeeCountByCategory.get(L1))
        .secondGradeMonthly(monthlyFeeCountByCategory.get(L2))
        .thirdGradeMonthly(monthlyFeeCountByCategory.get(L3))
        .firstGradeYearly(yearlyFeeCountByCategory.get(L1))
        .secondGradeYearly(yearlyFeeCountByCategory.get(L2))
        .thirdGradeYearly(yearlyFeeCountByCategory.get(L3))
        .unknownGrade(unknownGradeFees);
  }

  @Transactional
  public List<AdvancedFeeStats> updateAdvancedFeeStats(
      Optional<Instant> from, Optional<Instant> to, Optional<AdvancedFeeStatsCountType> type) {
    if (type.isEmpty()) {
      log.warn(
          "No count type provided for advanced stats generation request."
              + " ACCOUNTING stats will be generated by default");
    }
    return repository.saveAll(generateAdvancedFeeStats(from, to, type.orElse(ACCOUNTING)));
  }

  private Map<FeeCategory, Long> countFeesByGrades(List<Fee> fees, FeeFrequency frequency) {
    var feesByGradeCount = new HashMap<FeeCategory, Long>();
    Map<FeeCategory, List<Fee>> feesByGrade =
        fees.stream()
            .filter(fee -> !REMEDIAL_COSTS.equals(fee.getType()))
            .filter(fee -> fee.getFrequency() == frequency)
            .collect(groupingByConcurrent(Fee::getCategory));
    for (FeeCategory category : FeeCategory.values()) {
      feesByGradeCount.put(category, (long) feesByGrade.getOrDefault(category, List.of()).size());
    }
    return feesByGradeCount;
  }

  private Map<FeeFrequency, Long> countFeesByPaymentFrequency(List<Fee> fees) {
    var feesByPaymentFrequencyCount = new HashMap<FeeFrequency, Long>();
    Map<FeeFrequency, List<Fee>> feesByPaymentFrequency =
        fees.stream().collect(groupingByConcurrent(Fee::getFrequency));
    for (FeeFrequency feeFrequency : FeeFrequency.values()) {
      feesByPaymentFrequencyCount.put(
          feeFrequency, (long) feesByPaymentFrequency.getOrDefault(feeFrequency, List.of()).size());
    }
    return feesByPaymentFrequencyCount;
  }

  private Map<PaymentType, Long> countFeesByPaymentType(List<Fee> fees) {
    var feesByPaymentTypeCount = new HashMap<PaymentType, Long>();
    Map<PaymentType, List<Fee>> feeByPaymentType =
        fees.stream().collect(groupingByConcurrent(Fee::getPaymentType));
    for (PaymentType paymentType : PaymentType.values()) {
      feesByPaymentTypeCount.put(
          paymentType, (long) feeByPaymentType.getOrDefault(paymentType, List.of()).size());
    }
    return feesByPaymentTypeCount;
  }

  private long countRemedialFees(List<Fee> fees, FeeCategory feeCategory) {
    return fees.stream()
        .filter(
            fee -> REMEDIAL_COSTS.equals(fee.getType()) && feeCategory.equals(fee.getCategory()))
        .count();
  }

  private Map<FeeTypeEnum, List<Fee>> groupFeesByType(List<Fee> fees) {
    return fees.stream().collect(groupingBy(Fee::getType));
  }

  private List<Fee> filterFeesByStatus(
      List<Fee> fees, FeeStatusEnum feeStatus, Optional<LocalDate> statusDate) {
    return fees.stream()
        .filter(
            fee ->
                statusDate
                    .map(localDate -> feeStatusAtPredicate(fee, feeStatus, localDate))
                    .orElseGet(() -> feeStatusPredicate(fee, feeStatus)))
        .toList();
  }

  private boolean feeStatusPredicate(Fee fee, FeeStatusEnum feeStatus) {
    return feeStatus.equals(fee.getStatus());
  }

  private boolean feeStatusAtPredicate(Fee fee, FeeStatusEnum feeStatus, LocalDate date) {
    return feeStatus.equals(fee.getStatusAt(date.atStartOfDay().toInstant(UTC)).orElse(UNPAID));
  }
}
