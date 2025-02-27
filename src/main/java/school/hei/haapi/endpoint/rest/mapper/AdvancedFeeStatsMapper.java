package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.LATE_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PAID_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PENDING_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.TOTAL_COUNT;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.AdvancedFeesStatistics;
import school.hei.haapi.endpoint.rest.model.LateFeesStats;
import school.hei.haapi.endpoint.rest.model.PaidFeesStats;
import school.hei.haapi.endpoint.rest.model.PendingFeesStats;
import school.hei.haapi.endpoint.rest.model.TotalExpectedFeesStats;
import school.hei.haapi.model.statistics.AdvancedFeeStats;
import school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType;

@Component
public class AdvancedFeeStatsMapper {
  public Map<AdvancedFeeStatsType, AdvancedFeeStats> fromRest(AdvancedFeesStatistics restStat) {
    Map<AdvancedFeeStatsType, AdvancedFeeStats> statsModels = new HashMap<>();
    statsModels.put(LATE_COUNT, getModelLateFeeStats(restStat.getLateFeesCount()));
    statsModels.put(PAID_COUNT, getModelPaidFeeStats(restStat.getPaidFeesCount()));
    statsModels.put(PENDING_COUNT, getModelPendingFeeStats(restStat.getPendingFeesCount()));
    statsModels.put(TOTAL_COUNT, getModelTotalFeeStats(restStat.getTotalExpectedFeesCount()));
    return statsModels;
  }

  private AdvancedFeeStats getModelTotalFeeStats(TotalExpectedFeesStats restStat) {
    return AdvancedFeeStats.builder()
        .statType(TOTAL_COUNT)
        .firstGradeCount(restStat.getFirstGrade())
        .secondGradeCount(restStat.getSecondGrade())
        .thirdGradeCount(restStat.getThirdGrade())
        .remedialFeesCount(0L)
        .monthlyCount(restStat.getMonthly())
        .yearlyCount(restStat.getYearly())
        .workStudyCount(restStat.getWorkStudy())
        .build();
  }

  private AdvancedFeeStats getModelPaidFeeStats(PaidFeesStats restStat) {
    return AdvancedFeeStats.builder()
        .statType(PAID_COUNT)
        .firstGradeCount(restStat.getFirstGrade())
        .secondGradeCount(restStat.getSecondGrade())
        .thirdGradeCount(restStat.getThirdGrade())
        .remedialFeesCount(restStat.getRemedialFeesCount().longValue())
        .monthlyCount(restStat.getMonthly())
        .yearlyCount(restStat.getYearly())
        .workStudyCount(restStat.getWorkStudy())
        .bankTransferCount(restStat.getBankFees().longValue())
        .mpbsCount(restStat.getMobileMoney().longValue())
        .build();
  }

  private AdvancedFeeStats getModelPendingFeeStats(PendingFeesStats restStat) {
    return AdvancedFeeStats.builder()
        .statType(PENDING_COUNT)
        .firstGradeCount(restStat.getFirstGrade())
        .secondGradeCount(restStat.getSecondGrade())
        .thirdGradeCount(restStat.getThirdGrade())
        .remedialFeesCount(restStat.getRemedialFeesCount().longValue())
        .monthlyCount(restStat.getMonthly())
        .yearlyCount(restStat.getYearly())
        .workStudyCount(restStat.getWorkStudy())
        .build();
  }

  private AdvancedFeeStats getModelLateFeeStats(LateFeesStats restStat) {
    return AdvancedFeeStats.builder()
        .statType(LATE_COUNT)
        .firstGradeCount(restStat.getFirstGrade())
        .secondGradeCount(restStat.getSecondGrade())
        .thirdGradeCount(restStat.getThirdGrade())
        .monthlyCount(restStat.getMonthly())
        .remedialFeesCount(restStat.getRemedialFeesCount().longValue())
        .yearlyCount(restStat.getYearly())
        .workStudyCount(restStat.getWorkStudy())
        .build();
  }

  private LateFeesStats getRestLateFeeStats(AdvancedFeeStats modelStat) {
    return new LateFeesStats()
        .firstGrade(modelStat.getFirstGradeCount())
        .secondGrade(modelStat.getSecondGradeCount())
        .thirdGrade(modelStat.getThirdGradeCount())
        .remedialFeesCount(BigDecimal.valueOf(modelStat.getRemedialFeesCount()))
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount());
  }

  private PendingFeesStats getRestPendingFeeStats(AdvancedFeeStats modelStat) {
    return new PendingFeesStats()
        .firstGrade(modelStat.getFirstGradeCount())
        .secondGrade(modelStat.getSecondGradeCount())
        .thirdGrade(modelStat.getThirdGradeCount())
        .remedialFeesCount(BigDecimal.valueOf(modelStat.getRemedialFeesCount()))
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount());
  }

  private PaidFeesStats getRestPaidFeeStats(AdvancedFeeStats modelStat) {
    return new PaidFeesStats()
        .firstGrade(modelStat.getFirstGradeCount())
        .secondGrade(modelStat.getSecondGradeCount())
        .thirdGrade(modelStat.getThirdGradeCount())
        .remedialFeesCount(BigDecimal.valueOf(modelStat.getRemedialFeesCount()))
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount())
        .mobileMoney(BigDecimal.valueOf(modelStat.getMpbsCount()))
        .bankFees(BigDecimal.valueOf(modelStat.getBankTransferCount()));
  }

  private TotalExpectedFeesStats getRestTotalFeeStats(AdvancedFeeStats modelStat) {
    return new TotalExpectedFeesStats()
        .firstGrade(modelStat.getFirstGradeCount())
        .secondGrade(modelStat.getSecondGradeCount())
        .thirdGrade(modelStat.getThirdGradeCount())
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount());
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
