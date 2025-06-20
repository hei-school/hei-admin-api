package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.LATE_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PAID_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PENDING_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.TOTAL_COUNT;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.AdvancedFeesStatistics;
import school.hei.haapi.endpoint.rest.model.LateFeesStats;
import school.hei.haapi.endpoint.rest.model.PaidFeesStats;
import school.hei.haapi.endpoint.rest.model.PendingFeesStats;
import school.hei.haapi.endpoint.rest.model.TotalExpectedFeesStats;
import school.hei.haapi.model.statistics.AdvancedFeeStats;
import school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType;
import school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType;

@Component
public class AdvancedFeeStatsMapper {
  public Map<AdvancedFeeStatsType, AdvancedFeeStats> fromRest(
      AdvancedFeesStatistics restStat, LocalDate statStartDate, LocalDate statEndDate, AdvancedFeeStatsCountType type) {
    Map<AdvancedFeeStatsType, AdvancedFeeStats> statsModels = new HashMap<>();
    statsModels.put(
        LATE_COUNT, getModelLateFeeStats(restStat.getLateFeesCount(), statStartDate, statEndDate, type));
    statsModels.put(
        PAID_COUNT, getModelPaidFeeStats(restStat.getPaidFeesCount(), statStartDate, statEndDate, type));
    statsModels.put(
        PENDING_COUNT,
        getModelPendingFeeStats(restStat.getPendingFeesCount(), statStartDate, statEndDate, type));
    statsModels.put(
        TOTAL_COUNT,
        getModelTotalFeeStats(restStat.getTotalExpectedFeesCount(), statStartDate, statEndDate, type));
    return statsModels;
  }

  private AdvancedFeeStats getModelTotalFeeStats(
      TotalExpectedFeesStats restStat, LocalDate statStartDate, LocalDate statEndDate, AdvancedFeeStatsCountType type) {
    return AdvancedFeeStats.builder()
        .id(UUID.randomUUID().toString())
        .statType(TOTAL_COUNT)
        .firstGradeCount(restStat.getFirstGrade())
        .secondGradeCount(restStat.getSecondGrade())
        .thirdGradeCount(restStat.getThirdGrade())
        .unknownGradeCount(restStat.getUnknownGrade())
        .remedialFeesCount(0L)
        .monthlyCount(restStat.getMonthly())
        .yearlyCount(restStat.getYearly())
        .unknownFrequencyCount(restStat.getUnknownFrequency())
        .workStudyCount(restStat.getWorkStudy())
        .statStartDate(statStartDate)
        .statEndDate(statEndDate)
        .countType(type)
        .build();
  }

  private AdvancedFeeStats getModelPaidFeeStats(
      PaidFeesStats restStat, LocalDate statStartDate, LocalDate statEndDate, AdvancedFeeStatsCountType type) {
    return AdvancedFeeStats.builder()
        .id(UUID.randomUUID().toString())
        .statType(PAID_COUNT)
        .firstGradeCount(restStat.getFirstGrade())
        .secondGradeCount(restStat.getSecondGrade())
        .thirdGradeCount(restStat.getThirdGrade())
        .unknownGradeCount(restStat.getUnknownGrade())
        .remedialFeesCount(restStat.getRemedialFeesCount().longValue())
        .monthlyCount(restStat.getMonthly())
        .yearlyCount(restStat.getYearly())
        .unknownFrequencyCount(restStat.getUnknownFrequency())
        .workStudyCount(restStat.getWorkStudy())
        .bankTransferCount(restStat.getBankFees().longValue())
        .mpbsCount(restStat.getMobileMoney().longValue())
        .statStartDate(statStartDate)
        .statEndDate(statEndDate)
        .countType(type)
        .build();
  }

  private AdvancedFeeStats getModelPendingFeeStats(
      PendingFeesStats restStat, LocalDate statStartDate, LocalDate statEndDate, AdvancedFeeStatsCountType type) {
    return AdvancedFeeStats.builder()
        .id(UUID.randomUUID().toString())
        .statType(PENDING_COUNT)
        .firstGradeCount(restStat.getFirstGrade())
        .secondGradeCount(restStat.getSecondGrade())
        .thirdGradeCount(restStat.getThirdGrade())
        .unknownGradeCount(restStat.getUnknownGrade())
        .remedialFeesCount(restStat.getRemedialFeesCount().longValue())
        .monthlyCount(restStat.getMonthly())
        .yearlyCount(restStat.getYearly())
        .unknownFrequencyCount(restStat.getUnknownFrequency())
        .workStudyCount(restStat.getWorkStudy())
        .statStartDate(statStartDate)
        .statEndDate(statEndDate)
        .countType(type)
        .build();
  }

  private AdvancedFeeStats getModelLateFeeStats(
      LateFeesStats restStat, LocalDate statStartDate, LocalDate statEndDate, AdvancedFeeStatsCountType type) {
    return AdvancedFeeStats.builder()
        .id(UUID.randomUUID().toString())
        .statType(LATE_COUNT)
        .firstGradeCount(restStat.getFirstGrade())
        .secondGradeCount(restStat.getSecondGrade())
        .thirdGradeCount(restStat.getThirdGrade())
        .unknownGradeCount(restStat.getUnknownGrade())
        .monthlyCount(restStat.getMonthly())
        .yearlyCount(restStat.getYearly())
        .unknownFrequencyCount(restStat.getUnknownFrequency())
        .remedialFeesCount(restStat.getRemedialFeesCount().longValue())
        .workStudyCount(restStat.getWorkStudy())
        .statEndDate(statEndDate)
        .statStartDate(statStartDate)
        .countType(type)
        .build();
  }

  private LateFeesStats getRestLateFeeStats(AdvancedFeeStats modelStat) {
    return new LateFeesStats()
        .firstGrade(modelStat.getFirstGradeCount())
        .secondGrade(modelStat.getSecondGradeCount())
        .thirdGrade(modelStat.getThirdGradeCount())
        .unknownGrade(modelStat.getUnknownGradeCount())
        .remedialFeesCount(BigDecimal.valueOf(modelStat.getRemedialFeesCount()))
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount())
        .unknownFrequency(modelStat.getUnknownFrequencyCount());
  }

  private PendingFeesStats getRestPendingFeeStats(AdvancedFeeStats modelStat) {
    return new PendingFeesStats()
        .firstGrade(modelStat.getFirstGradeCount())
        .secondGrade(modelStat.getSecondGradeCount())
        .thirdGrade(modelStat.getThirdGradeCount())
        .unknownGrade(modelStat.getUnknownGradeCount())
        .remedialFeesCount(BigDecimal.valueOf(modelStat.getRemedialFeesCount()))
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount())
        .unknownFrequency(modelStat.getUnknownFrequencyCount());
  }

  private PaidFeesStats getRestPaidFeeStats(AdvancedFeeStats modelStat) {
    return new PaidFeesStats()
        .firstGrade(modelStat.getFirstGradeCount())
        .secondGrade(modelStat.getSecondGradeCount())
        .thirdGrade(modelStat.getThirdGradeCount())
        .unknownGrade(modelStat.getUnknownGradeCount())
        .remedialFeesCount(BigDecimal.valueOf(modelStat.getRemedialFeesCount()))
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount())
        .unknownFrequency(modelStat.getUnknownFrequencyCount())
        .mobileMoney(BigDecimal.valueOf(modelStat.getMpbsCount()))
        .bankFees(BigDecimal.valueOf(modelStat.getBankTransferCount()));
  }

  private TotalExpectedFeesStats getRestTotalFeeStats(AdvancedFeeStats modelStat) {
    return new TotalExpectedFeesStats()
        .firstGrade(modelStat.getFirstGradeCount())
        .secondGrade(modelStat.getSecondGradeCount())
        .thirdGrade(modelStat.getThirdGradeCount())
        .unknownGrade(modelStat.getUnknownGradeCount())
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount())
        .unknownFrequency(modelStat.getUnknownFrequencyCount());
  }

  public AdvancedFeesStatistics toRest(Map<AdvancedFeeStatsType, AdvancedFeeStats> modelStat) {
    AdvancedFeesStatistics restStat = new AdvancedFeesStatistics();
    for (Entry<AdvancedFeeStatsType, AdvancedFeeStats> entry : modelStat.entrySet()) {
      switch (entry.getKey()) {
        case PENDING_COUNT -> restStat.pendingFeesCount(getRestPendingFeeStats(entry.getValue()));
        case LATE_COUNT -> restStat.lateFeesCount(getRestLateFeeStats(entry.getValue()));
        case PAID_COUNT -> restStat.paidFeesCount(getRestPaidFeeStats(entry.getValue()));
        case TOTAL_COUNT -> restStat.totalExpectedFeesCount(getRestTotalFeeStats(entry.getValue()));
      }
    }

    return restStat;
  }
}
