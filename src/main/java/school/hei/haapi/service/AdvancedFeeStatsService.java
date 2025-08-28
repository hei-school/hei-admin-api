package school.hei.haapi.service;

import static java.time.Instant.now;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
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
import static school.hei.haapi.model.fee.PaymentType.BANK;
import static school.hei.haapi.model.fee.PaymentType.MPBS;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType.ACCOUNTING;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.AdvancedFeeStatsComputationTriggered;
import school.hei.haapi.endpoint.rest.mapper.AdvancedFeeStatsMapper;
import school.hei.haapi.endpoint.rest.model.AdvancedFeesStatistics;
import school.hei.haapi.endpoint.rest.model.FeeCategory;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.endpoint.rest.model.LateFeesStats;
import school.hei.haapi.endpoint.rest.model.PaidFeesStats;
import school.hei.haapi.endpoint.rest.model.PendingFeesStats;
import school.hei.haapi.endpoint.rest.model.TotalExpectedFeesStats;
import school.hei.haapi.model.Fee;
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

  private static final Duration ADVANCED_FEE_STATS_EXPIRATION = Duration.of(3, DAYS);

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
      eventProducer.accept(
          List.of(
              new AdvancedFeeStatsComputationTriggered(
                  from.atStartOfDay(), to.atTime(23, 59, 59), type)));
      return new AdvancedFeesStatistics().expired(true);
    }

    if (shouldBeUpdated(advancedStats)) {
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
              case PENDING_COUNT -> handlePendingFeesCount(stat, restStats.getPendingFeesCount());
              case LATE_COUNT -> handleLateFeesCount(stat, restStats.getLateFeesCount());
              case PAID_COUNT -> handlePaidFeesCount(stat, restStats.getPaidFeesCount());
              case TOTAL_COUNT -> handleTotalFeesCount(stat, restStats.getTotalExpectedFeesCount());
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
        .totalExpectedFeesCount(getTotalExpectedFeesStats(fees));
  }

  private void handlePendingFeesCount(
      AdvancedFeeStats feeStats, PendingFeesStats pendingFeesStats) {
    feeStats.setFirstGradeCount(pendingFeesStats.getFirstGrade());
    feeStats.setSecondGradeCount(pendingFeesStats.getSecondGrade());
    feeStats.setThirdGradeCount(pendingFeesStats.getThirdGrade());
    feeStats.setUnknownGradeCount(pendingFeesStats.getUnknownGrade());
    feeStats.setWorkStudyCount(pendingFeesStats.getWorkStudy());
    feeStats.setRemedialFeesCount(pendingFeesStats.getRemedialFeesCount().longValue());
    feeStats.setMonthlyCount(pendingFeesStats.getMonthly());
    feeStats.setYearlyCount(pendingFeesStats.getYearly());
    feeStats.setUpdateDatetime(now());
    feeStats.setUnknownFrequencyCount(pendingFeesStats.getUnknownFrequency());
  }

  private void handleLateFeesCount(AdvancedFeeStats feeStats, LateFeesStats lateFeesStats) {
    feeStats.setFirstGradeCount(lateFeesStats.getFirstGrade());
    feeStats.setSecondGradeCount(lateFeesStats.getSecondGrade());
    feeStats.setThirdGradeCount(lateFeesStats.getThirdGrade());
    feeStats.setUnknownGradeCount(lateFeesStats.getUnknownGrade());
    feeStats.setWorkStudyCount(lateFeesStats.getWorkStudy());
    feeStats.setRemedialFeesCount(lateFeesStats.getRemedialFeesCount().longValue());
    feeStats.setMonthlyCount(lateFeesStats.getMonthly());
    feeStats.setYearlyCount(lateFeesStats.getYearly());
    feeStats.setUpdateDatetime(now());
    feeStats.setUnknownFrequencyCount(lateFeesStats.getUnknownFrequency());
  }

  private void handlePaidFeesCount(AdvancedFeeStats feeStats, PaidFeesStats paidFeesStats) {
    feeStats.setFirstGradeCount(paidFeesStats.getFirstGrade());
    feeStats.setSecondGradeCount(paidFeesStats.getSecondGrade());
    feeStats.setThirdGradeCount(paidFeesStats.getThirdGrade());
    feeStats.setUnknownGradeCount(paidFeesStats.getUnknownGrade());
    feeStats.setWorkStudyCount(paidFeesStats.getWorkStudy());
    feeStats.setRemedialFeesCount(paidFeesStats.getRemedialFeesCount().longValue());
    feeStats.setMonthlyCount(paidFeesStats.getMonthly());
    feeStats.setYearlyCount(paidFeesStats.getYearly());
    feeStats.setUnknownFrequencyCount(paidFeesStats.getUnknownFrequency());
    feeStats.setBankTransferCount(paidFeesStats.getBankFees().longValue());
    feeStats.setMpbsCount(paidFeesStats.getMobileMoney().longValue());
    feeStats.setUpdateDatetime(now());
  }

  private void handleTotalFeesCount(
      AdvancedFeeStats feeStats, TotalExpectedFeesStats totalExpectedFeesStats) {
    feeStats.setFirstGradeCount(totalExpectedFeesStats.getFirstGrade());
    feeStats.setSecondGradeCount(totalExpectedFeesStats.getSecondGrade());
    feeStats.setThirdGradeCount(totalExpectedFeesStats.getThirdGrade());
    feeStats.setUnknownGradeCount(totalExpectedFeesStats.getUnknownGrade());
    feeStats.setWorkStudyCount(totalExpectedFeesStats.getWorkStudy());
    feeStats.setMonthlyCount(totalExpectedFeesStats.getMonthly());
    feeStats.setYearlyCount(totalExpectedFeesStats.getYearly());
    feeStats.setUpdateDatetime(now());
    feeStats.setUnknownFrequencyCount(totalExpectedFeesStats.getUnknownFrequency());
  }

  private LateFeesStats getLateFeesStats(List<Fee> fees, Optional<LocalDate> statusDate) {
    List<Fee> lateFees = filterFeesByStatus(fees, LATE, statusDate);
    Map<FeeCategory, Long> feeCountByCategory = countFeesByGrades(lateFees);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(lateFees);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<FeeFrequency, Long> feesCountByPaymentFrequency = countFeesByPaymentFrequency(tuitionFees);
    return new LateFeesStats()
        .remedialFeesCount(BigDecimal.valueOf(countRemedialFees(lateFees)))
        .workStudy(feeCountByCategory.get(WORK_FEES))
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .unknownFrequency(feesCountByPaymentFrequency.get(UNKNOWN))
        .firstGrade(feeCountByCategory.get(L1))
        .secondGrade(feeCountByCategory.get(L2))
        .thirdGrade(feeCountByCategory.get(L3))
        .unknownGrade(feeCountByCategory.get(FeeCategory.UNKNOWN));
  }

  private PaidFeesStats getPaidFeesStats(List<Fee> fees, Optional<LocalDate> statusDate) {
    List<Fee> paidFees = filterFeesByStatus(fees, PAID, statusDate);
    Map<FeeCategory, Long> feeCountByCategory = countFeesByGrades(paidFees);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(paidFees);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<FeeFrequency, Long> feesCountByPaymentFrequency = countFeesByPaymentFrequency(tuitionFees);
    Map<PaymentType, Long> feesCountByPaymentType = countFeesByPaymentType(tuitionFees);
    return new PaidFeesStats()
        .remedialFeesCount(BigDecimal.valueOf(countRemedialFees(paidFees)))
        .workStudy(feeCountByCategory.get(WORK_FEES))
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .unknownFrequency(feesCountByPaymentFrequency.get(UNKNOWN))
        .firstGrade(feeCountByCategory.get(L1))
        .secondGrade(feeCountByCategory.get(L2))
        .thirdGrade(feeCountByCategory.get(L3))
        .unknownGrade(feeCountByCategory.get(FeeCategory.UNKNOWN))
        .bankFees(BigDecimal.valueOf(feesCountByPaymentType.get(BANK)))
        .mobileMoney(BigDecimal.valueOf(feesCountByPaymentType.get(MPBS)));
  }

  private PendingFeesStats getPendingFeesStats(List<Fee> fees, Optional<LocalDate> statusDate) {
    List<Fee> pendingFees = filterFeesByStatus(fees, PENDING, statusDate);
    Map<FeeCategory, Long> feeCountByCategory = countFeesByGrades(pendingFees);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(pendingFees);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<FeeFrequency, Long> feesCountByPaymentFrequency = countFeesByPaymentFrequency(tuitionFees);
    return new PendingFeesStats()
        .remedialFeesCount(BigDecimal.valueOf(countRemedialFees(pendingFees)))
        .workStudy(feeCountByCategory.get(WORK_FEES))
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .unknownFrequency(feesCountByPaymentFrequency.get(UNKNOWN))
        .firstGrade(feeCountByCategory.get(L1))
        .secondGrade(feeCountByCategory.get(L2))
        .thirdGrade(feeCountByCategory.get(L3))
        .unknownGrade(feeCountByCategory.get(FeeCategory.UNKNOWN));
  }

  private TotalExpectedFeesStats getTotalExpectedFeesStats(List<Fee> fees) {
    Map<FeeCategory, Long> feeCountByCategory = countFeesByGrades(fees);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(fees);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<FeeFrequency, Long> feesCountByPaymentFrequency = countFeesByPaymentFrequency(tuitionFees);
    return new TotalExpectedFeesStats()
        .firstGrade(feeCountByCategory.get(L1))
        .secondGrade(feeCountByCategory.get(L2))
        .thirdGrade(feeCountByCategory.get(L3))
        .unknownGrade(feeCountByCategory.get(FeeCategory.UNKNOWN))
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .unknownFrequency(feesCountByPaymentFrequency.get(UNKNOWN))
        .workStudy(feeCountByCategory.get(WORK_FEES));
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

  private Map<FeeCategory, Long> countFeesByGrades(List<Fee> fees) {
    var feesByGradeCount = new HashMap<FeeCategory, Long>();
    Map<FeeCategory, List<Fee>> feesByGrade =
        fees.stream().collect(groupingByConcurrent(Fee::getCategory));
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

  private long countRemedialFees(List<Fee> fees) {
    return fees.stream().filter(fee -> REMEDIAL_COSTS.equals(fee.getType())).count();
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
