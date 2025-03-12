package school.hei.haapi.service;

import static java.time.LocalTime.MAX;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.groupingByConcurrent;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.REMEDIAL_COSTS;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.endpoint.rest.model.PaymentFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.PaymentFrequency.YEARLY;
import static school.hei.haapi.model.fee.PaymentType.BANK;
import static school.hei.haapi.model.fee.PaymentType.MPBS;
import static school.hei.haapi.model.fee.StudentGrade.L1;
import static school.hei.haapi.model.fee.StudentGrade.L2;
import static school.hei.haapi.model.fee.StudentGrade.L3;

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
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.endpoint.rest.model.FeeTypeEnum;
import school.hei.haapi.endpoint.rest.model.LateFeesStats;
import school.hei.haapi.endpoint.rest.model.PaidFeesStats;
import school.hei.haapi.endpoint.rest.model.PaymentFrequency;
import school.hei.haapi.endpoint.rest.model.PendingFeesStats;
import school.hei.haapi.endpoint.rest.model.TotalExpectedFeesStats;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.fee.PaymentType;
import school.hei.haapi.model.fee.StudentGrade;
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
    LocalDate to =
        Optional.ofNullable(dateTo)
            .orElse(now.withDayOfMonth(now.getMonth().length(now.isLeapYear())));
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
        .forEach(
            date -> {
              Instant dayStart = date.atStartOfDay().toInstant(UTC);
              Instant dayEnd = date.atTime(MAX).toInstant(UTC);

              List<Fee> allFees = feeRepository.findAllByDueDatetimeBetween(dayStart, dayEnd);

              AdvancedFeesStatistics restStats =
                  new AdvancedFeesStatistics()
                      .lateFeesCount(getLateFeesStats(allFees))
                      .paidFeesCount(getPaidFeesStats(allFees))
                      .pendingFeesCount(getPendingFeesStats(allFees))
                      .totalExpectedFeesCount(getTotalExpectedFeesStats(allFees));

              Map<AdvancedFeeStats.AdvancedFeeStatsType, AdvancedFeeStats> dayStat =
                  advancedFeeStatsMapper.fromRest(restStats, date);

              Collection<AdvancedFeeStats> stats = feeDao.getAdvancedFeeStats(date).values();
              if (!stats.isEmpty()) {
                stats.forEach(
                    stat -> {
                      switch (stat.getStatType()) {
                        case PENDING_COUNT ->
                            handlePendingFeesCount(stat, restStats.getPendingFeesCount());
                        case LATE_COUNT -> handleLateFeesCount(stat, restStats.getLateFeesCount());
                        case PAID_COUNT -> handlePaidFeesCount(stat, restStats.getPaidFeesCount());
                        case TOTAL_COUNT ->
                            handleTotalFeesCount(stat, restStats.getTotalExpectedFeesCount());
                      }
                    });
                statistics.addAll(stats);
              } else {
                statistics.addAll(dayStat.values());
              }
            });

    return statistics;
  }

  private void handlePendingFeesCount(
      AdvancedFeeStats feeStats, PendingFeesStats pendingFeesStats) {
    feeStats.setFirstGradeCount(pendingFeesStats.getFirstGrade());
    feeStats.setSecondGradeCount(pendingFeesStats.getSecondGrade());
    feeStats.setThirdGradeCount(pendingFeesStats.getThirdGrade());
    feeStats.setWorkStudyCount(pendingFeesStats.getWorkStudy());
    feeStats.setRemedialFeesCount(pendingFeesStats.getRemedialFeesCount().longValue());
    feeStats.setMonthlyCount(pendingFeesStats.getMonthly());
    feeStats.setYearlyCount(pendingFeesStats.getYearly());
  }

  private void handleLateFeesCount(AdvancedFeeStats feeStats, LateFeesStats lateFeesStats) {
    feeStats.setFirstGradeCount(lateFeesStats.getFirstGrade());
    feeStats.setSecondGradeCount(lateFeesStats.getSecondGrade());
    feeStats.setThirdGradeCount(lateFeesStats.getThirdGrade());
    feeStats.setWorkStudyCount(lateFeesStats.getWorkStudy());
    feeStats.setRemedialFeesCount(lateFeesStats.getRemedialFeesCount().longValue());
    feeStats.setMonthlyCount(lateFeesStats.getMonthly());
    feeStats.setYearlyCount(lateFeesStats.getYearly());
  }

  private void handlePaidFeesCount(AdvancedFeeStats feeStats, PaidFeesStats paidFeesStats) {
    feeStats.setFirstGradeCount(paidFeesStats.getFirstGrade());
    feeStats.setSecondGradeCount(paidFeesStats.getSecondGrade());
    feeStats.setThirdGradeCount(paidFeesStats.getThirdGrade());
    feeStats.setWorkStudyCount(paidFeesStats.getWorkStudy());
    feeStats.setRemedialFeesCount(paidFeesStats.getRemedialFeesCount().longValue());
    feeStats.setMonthlyCount(paidFeesStats.getMonthly());
    feeStats.setYearlyCount(paidFeesStats.getYearly());
    feeStats.setBankTransferCount(paidFeesStats.getBankFees().longValue());
    feeStats.setMpbsCount(paidFeesStats.getMobileMoney().longValue());
  }

  private void handleTotalFeesCount(
      AdvancedFeeStats feeStats, TotalExpectedFeesStats totalExpectedFeesStats) {
    feeStats.setFirstGradeCount(totalExpectedFeesStats.getFirstGrade());
    feeStats.setSecondGradeCount(totalExpectedFeesStats.getSecondGrade());
    feeStats.setThirdGradeCount(totalExpectedFeesStats.getThirdGrade());
    feeStats.setWorkStudyCount(totalExpectedFeesStats.getWorkStudy());
    feeStats.setMonthlyCount(totalExpectedFeesStats.getMonthly());
    feeStats.setYearlyCount(totalExpectedFeesStats.getYearly());
  }

  private LateFeesStats getLateFeesStats(List<Fee> fees) {
    List<Fee> lateFees = filterFeesByStatus(fees, LATE);
    Map<StudentGrade, Long> feeCountByGrade = countFeesByGrades(lateFees);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(lateFees);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<PaymentFrequency, Long> feesCountByPaymentFrequency =
        countFeesByPaymentFrequency(tuitionFees);
    return new LateFeesStats()
        .remedialFeesCount(BigDecimal.valueOf(countRemedialFees(lateFees)))
        .workStudy(countWorkStudyFees(tuitionFees))
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .firstGrade(feeCountByGrade.get(L1))
        .secondGrade(feeCountByGrade.get(L2))
        .thirdGrade(feeCountByGrade.get(L3));
  }

  private PaidFeesStats getPaidFeesStats(List<Fee> fees) {
    List<Fee> paidFees = filterFeesByStatus(fees, PAID);
    Map<StudentGrade, Long> feeCountByGrade = countFeesByGrades(paidFees);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(paidFees);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<PaymentFrequency, Long> feesCountByPaymentFrequency =
        countFeesByPaymentFrequency(tuitionFees);
    Map<PaymentType, Long> feesCountByPaymentType = countFeesByPaymentType(tuitionFees);
    return new PaidFeesStats()
        .remedialFeesCount(BigDecimal.valueOf(countRemedialFees(paidFees)))
        .workStudy(countWorkStudyFees(tuitionFees))
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .firstGrade(feeCountByGrade.get(L1))
        .secondGrade(feeCountByGrade.get(L2))
        .thirdGrade(feeCountByGrade.get(L3))
        .bankFees(BigDecimal.valueOf(feesCountByPaymentType.get(BANK)))
        .mobileMoney(BigDecimal.valueOf(feesCountByPaymentType.get(MPBS)));
  }

  private PendingFeesStats getPendingFeesStats(List<Fee> fees) {
    List<Fee> pendingFees = filterFeesByStatus(fees, PENDING);
    Map<StudentGrade, Long> feeCountByGrade = countFeesByGrades(pendingFees);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(pendingFees);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<PaymentFrequency, Long> feesCountByPaymentFrequency =
        countFeesByPaymentFrequency(tuitionFees);
    return new PendingFeesStats()
        .remedialFeesCount(BigDecimal.valueOf(countRemedialFees(pendingFees)))
        .workStudy(countWorkStudyFees(tuitionFees))
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .firstGrade(feeCountByGrade.get(L1))
        .secondGrade(feeCountByGrade.get(L2))
        .thirdGrade(feeCountByGrade.get(L3));
  }

  private TotalExpectedFeesStats getTotalExpectedFeesStats(List<Fee> fees) {
    Map<StudentGrade, Long> feeCountByGrade = countFeesByGrades(fees);
    Map<FeeTypeEnum, List<Fee>> feesByType = groupFeesByType(fees);
    List<Fee> tuitionFees = feesByType.getOrDefault(TUITION, List.of());
    Map<PaymentFrequency, Long> feesCountByPaymentFrequency =
        countFeesByPaymentFrequency(tuitionFees);
    return new TotalExpectedFeesStats()
        .firstGrade(feeCountByGrade.get(L1))
        .secondGrade(feeCountByGrade.get(L2))
        .thirdGrade(feeCountByGrade.get(L3))
        .monthly(feesCountByPaymentFrequency.get(MONTHLY))
        .yearly(feesCountByPaymentFrequency.get(YEARLY))
        .workStudy(countWorkStudyFees(tuitionFees));
  }

  @Transactional
  public List<AdvancedFeeStats> updateAdvancedFeeStats(
      Optional<Instant> from, Optional<Instant> to) {
    return repository.saveAll(generateAdvancedFeeStats(from, to));
  }

  private Map<StudentGrade, Long> countFeesByGrades(List<Fee> fees) {
    var feesByGradeCount = new HashMap<StudentGrade, Long>();
    Map<Optional<StudentGrade>, List<Fee>> feesByGrade =
        fees.stream()
            .filter(fee -> fee.getOwnerStudentGrade().isPresent())
            .collect(groupingByConcurrent(Fee::getOwnerStudentGrade));
    for (StudentGrade grade : StudentGrade.values()) {
      feesByGradeCount.put(
          grade, (long) feesByGrade.getOrDefault(Optional.of(grade), List.of()).size());
    }
    return feesByGradeCount;
  }

  private Map<PaymentFrequency, Long> countFeesByPaymentFrequency(List<Fee> fees) {
    var feesByPaymentFrequencyCount = new HashMap<PaymentFrequency, Long>();
    Map<Optional<PaymentFrequency>, List<Fee>> feesByPaymentFrequency =
        fees.stream()
            .filter(fee -> fee.getPaymentFrequency().isPresent())
            .collect(groupingByConcurrent(Fee::getPaymentFrequency));
    for (PaymentFrequency paymentFrequency : PaymentFrequency.values()) {
      feesByPaymentFrequencyCount.put(
          paymentFrequency,
          (long)
              feesByPaymentFrequency.getOrDefault(Optional.of(paymentFrequency), List.of()).size());
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

  private long countWorkStudyFees(List<Fee> fees) {
    return fees.stream().filter(Fee::isWorkStudyStudentFee).count();
  }

  private Map<FeeTypeEnum, List<Fee>> groupFeesByType(List<Fee> fees) {
    return fees.stream().collect(groupingBy(Fee::getType));
  }

  private List<Fee> filterFeesByStatus(List<Fee> fees, FeeStatusEnum feeStatus) {
    return fees.stream().filter(fee -> feeStatus.equals(fee.getStatus())).toList();
  }
}
