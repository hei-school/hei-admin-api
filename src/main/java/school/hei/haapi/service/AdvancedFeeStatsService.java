package school.hei.haapi.service;

import static java.time.LocalTime.MAX;
import static java.time.ZoneOffset.UTC;
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
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.REMEDIAL_COSTS;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.model.fee.PaymentType.BANK;
import static school.hei.haapi.model.fee.PaymentType.MPBS;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import school.hei.haapi.repository.AdvancedFeeStatsRepository;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.dao.FeeDao;
import school.hei.haapi.service.utils.DateUtils;

@Service
@RequiredArgsConstructor
public class AdvancedFeeStatsService {
  private final FeeDao feeDao;
  private final AdvancedFeeStatsRepository repository;
  private final AdvancedFeeStatsMapper advancedFeeStatsMapper;
  private final FeeRepository feeRepository;

  public AdvancedFeesStatistics getAdvancedFeeStats(LocalDate dateFrom, LocalDate dateTo) {
    LocalDate now = LocalDate.now();
    LocalDate from = Optional.ofNullable(dateFrom).orElse(now.withDayOfMonth(1));
    LocalDate to = Optional.ofNullable(dateTo).orElse(now.with(lastDayOfMonth()).plusDays(1));
    return advancedFeeStatsMapper.toRest(feeDao.getAdvancedFeeStats(from, to));
  }

  public List<AdvancedFeeStats> generateAdvancedFeeStats(
      Optional<Instant> fromInstant, Optional<Instant> toInstant) {
    List<AdvancedFeeStats> statistics = new ArrayList<>();
    Instant startOfDay = LocalDate.now().atStartOfDay().toInstant(UTC);
    Instant endOfDay = LocalDate.now().atTime(MAX).toInstant(UTC);

    DateUtils.RangedInstant currentDayRange =
        new DateUtils.RangedInstant(fromInstant.orElse(startOfDay), toInstant.orElse(endOfDay));
    LocalDate fromDate = currentDayRange.from().atZone(UTC).toLocalDate();
    LocalDate toDate = currentDayRange.to().atZone(UTC).toLocalDate();

    fromDate
        .datesUntil(toDate.plusDays(1))
        .forEach(date -> statistics.addAll(generateAdvancedFeeStatsOnDate(date)));
    return statistics;
  }

  private Collection<AdvancedFeeStats> generateAdvancedFeeStatsOnDate(LocalDate date) {
    Instant dayStart = date.atStartOfDay().toInstant(UTC);
    Instant dayEnd = date.atTime(MAX).toInstant(UTC);

    List<Fee> allFees = feeRepository.findAllByUpdatedAtBetween(dayStart, dayEnd);

    Collection<AdvancedFeeStats> stats = feeDao.getAdvancedFeeStatsOnDate(date).values();
    AdvancedFeesStatistics restStats = generateAdvancedFeeStats(allFees);
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

    return advancedFeeStatsMapper.fromRest(restStats, date).values();
  }

  private AdvancedFeesStatistics generateAdvancedFeeStats(List<Fee> fees) {
    return new AdvancedFeesStatistics()
        .lateFeesCount(getLateFeesStats(fees))
        .paidFeesCount(getPaidFeesStats(fees))
        .pendingFeesCount(getPendingFeesStats(fees))
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
    feeStats.setUnknownFrequencyCount(totalExpectedFeesStats.getUnknownFrequency());
  }

  private LateFeesStats getLateFeesStats(List<Fee> fees) {
    List<Fee> lateFees = filterFeesByStatus(fees, LATE);
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

  private PaidFeesStats getPaidFeesStats(List<Fee> fees) {
    List<Fee> paidFees = filterFeesByStatus(fees, PAID);
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

  private PendingFeesStats getPendingFeesStats(List<Fee> fees) {
    List<Fee> pendingFees = filterFeesByStatus(fees, PENDING);
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
      Optional<Instant> from, Optional<Instant> to) {
    return repository.saveAll(generateAdvancedFeeStats(from, to));
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

  private List<Fee> filterFeesByStatus(List<Fee> fees, FeeStatusEnum feeStatus) {
    return fees.stream().filter(fee -> feeStatus.equals(fee.getStatus())).toList();
  }
}
