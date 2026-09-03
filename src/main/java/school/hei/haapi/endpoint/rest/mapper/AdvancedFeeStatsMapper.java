package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.LATE_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PAID_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PENDING_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.TOTAL_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.UNPAID_COUNT;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.AdvancedFeesStatistics;
import school.hei.haapi.endpoint.rest.model.LateFeesStats;
import school.hei.haapi.endpoint.rest.model.PaidFeesStats;
import school.hei.haapi.endpoint.rest.model.PendingFeesStats;
import school.hei.haapi.endpoint.rest.model.TotalExpectedFeesStats;
import school.hei.haapi.endpoint.rest.model.UnpaidFeesStats;
import school.hei.haapi.model.statistics.AdvancedFeeStats;
import school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType;
import school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType;

@Slf4j
@Component
public class AdvancedFeeStatsMapper {
  public Map<AdvancedFeeStatsType, AdvancedFeeStats> fromRest(
      AdvancedFeesStatistics restStat,
      LocalDate statStartDate,
      LocalDate statEndDate,
      AdvancedFeeStatsCountType type) {
    Map<AdvancedFeeStatsType, AdvancedFeeStats> statsModels = new HashMap<>();
    statsModels.put(
        LATE_COUNT,
        getModelLateFeeStats(restStat.getLateFeesCount(), statStartDate, statEndDate, type));
    statsModels.put(
        PAID_COUNT,
        getModelPaidFeeStats(restStat.getPaidFeesCount(), statStartDate, statEndDate, type));
    statsModels.put(
        PENDING_COUNT,
        getModelPendingFeeStats(restStat.getPendingFeesCount(), statStartDate, statEndDate, type));
    statsModels.put(
        UNPAID_COUNT,
        getModelUnpaidFeeStats(restStat.getUnpaidFeesCount(), statStartDate, statEndDate, type));
    statsModels.put(
        TOTAL_COUNT,
        getModelTotalFeeStats(
            restStat.getTotalExpectedFeesCount(), statStartDate, statEndDate, type));
    return statsModels;
  }

  private AdvancedFeeStats getModelTotalFeeStats(
      TotalExpectedFeesStats restStat,
      LocalDate statStartDate,
      LocalDate statEndDate,
      AdvancedFeeStatsCountType type) {
    return AdvancedFeeStats.builder()
        .id(UUID.randomUUID().toString())
        .statType(TOTAL_COUNT)
        .firstGradeCountMonthly(safe(restStat.getFirstGradeMonthly()))
        .secondGradeCountMonthly(safe(restStat.getSecondGradeMonthly()))
        .thirdGradeCountMonthly(safe(restStat.getThirdGradeMonthly()))
        .firstGradeCountYearly(safe(restStat.getFirstGradeYearly()))
        .secondGradeCountYearly(safe(restStat.getSecondGradeYearly()))
        .thirdGradeCountYearly(safe(restStat.getThirdGradeYearly()))
        .unknownGradeCount(safe(restStat.getUnknownGrade()))
        .retakeExamFirstGradeCount(safe(restStat.getRetakeExamFirstGradeCount()))
        .retakeExamSecondGradeCount(safe(restStat.getRetakeExamSecondGradeCount()))
        .retakeExamThirdGradeCount(safe(restStat.getRetakeExamThirdGradeCount()))
        .studentInsuranceFirstGradeCount(safe(restStat.getStudentInsuranceFirstGradeCount()))
        .studentInsuranceSecondGradeCount(safe(restStat.getStudentInsuranceSecondGradeCount()))
        .studentInsuranceThirdGradeCount(safe(restStat.getStudentInsuranceThirdGradeCount()))
        .monthlyCount(safe(restStat.getMonthly()))
        .yearlyCount(safe(restStat.getYearly()))
        .unknownFrequencyCount(safe(restStat.getUnknownFrequency()))
        .workStudyCount(safe(restStat.getWorkStudy()))
        .statStartDate(statStartDate)
        .statEndDate(statEndDate)
        .countType(type)
        .build();
  }

  private AdvancedFeeStats getModelPaidFeeStats(
      PaidFeesStats restStat,
      LocalDate statStartDate,
      LocalDate statEndDate,
      AdvancedFeeStatsCountType type) {

    return AdvancedFeeStats.builder()
        .id(UUID.randomUUID().toString())
        .statType(PAID_COUNT)
        .firstGradeCountMonthly(safe(restStat.getFirstGradeMonthly()))
        .secondGradeCountMonthly(safe(restStat.getSecondGradeMonthly()))
        .thirdGradeCountMonthly(safe(restStat.getThirdGradeMonthly()))
        .firstGradeCountYearly(safe(restStat.getFirstGradeYearly()))
        .secondGradeCountYearly(safe(restStat.getSecondGradeYearly()))
        .thirdGradeCountYearly(safe(restStat.getThirdGradeYearly()))
        .unknownGradeCount(safe(restStat.getUnknownGrade()))
        .retakeExamFirstGradeCount(safe(restStat.getRetakeExamFirstGradeCount()))
        .retakeExamSecondGradeCount(safe(restStat.getRetakeExamSecondGradeCount()))
        .retakeExamThirdGradeCount(safe(restStat.getRetakeExamThirdGradeCount()))
        .studentInsuranceFirstGradeCount(safe(restStat.getStudentInsuranceFirstGradeCount()))
        .studentInsuranceSecondGradeCount(safe(restStat.getStudentInsuranceSecondGradeCount()))
        .studentInsuranceThirdGradeCount(safe(restStat.getStudentInsuranceThirdGradeCount()))
        .monthlyCount(safe(restStat.getMonthly()))
        .yearlyCount(safe(restStat.getYearly()))
        .unknownFrequencyCount(safe(restStat.getUnknownFrequency()))
        .workStudyCount(safe(restStat.getWorkStudy()))
        .bankTransferCount(safeBigDecimal(restStat.getBankFees()))
        .mpbsCount(safeBigDecimal(restStat.getMobileMoney()))
        .statStartDate(statStartDate)
        .statEndDate(statEndDate)
        .countType(type)
        .build();
  }

  private AdvancedFeeStats getModelPendingFeeStats(
      PendingFeesStats restStat,
      LocalDate statStartDate,
      LocalDate statEndDate,
      AdvancedFeeStatsCountType type) {
    return AdvancedFeeStats.builder()
        .id(UUID.randomUUID().toString())
        .statType(PENDING_COUNT)
        .firstGradeCountMonthly(safe(restStat.getFirstGradeMonthly()))
        .secondGradeCountMonthly(safe(restStat.getSecondGradeMonthly()))
        .thirdGradeCountMonthly(safe(restStat.getThirdGradeMonthly()))
        .firstGradeCountYearly(safe(restStat.getFirstGradeYearly()))
        .secondGradeCountYearly(safe(restStat.getSecondGradeYearly()))
        .thirdGradeCountYearly(safe(restStat.getThirdGradeYearly()))
        .unknownGradeCount(safe(restStat.getUnknownGrade()))
        .retakeExamFirstGradeCount(safe(restStat.getRetakeExamFirstGradeCount()))
        .retakeExamSecondGradeCount(safe(restStat.getRetakeExamSecondGradeCount()))
        .retakeExamThirdGradeCount(safe(restStat.getRetakeExamThirdGradeCount()))
        .studentInsuranceFirstGradeCount(safe(restStat.getStudentInsuranceFirstGradeCount()))
        .studentInsuranceSecondGradeCount(safe(restStat.getStudentInsuranceSecondGradeCount()))
        .studentInsuranceThirdGradeCount(safe(restStat.getStudentInsuranceThirdGradeCount()))
        .monthlyCount(safe(restStat.getMonthly()))
        .yearlyCount(safe(restStat.getYearly()))
        .unknownFrequencyCount(safe(restStat.getUnknownFrequency()))
        .workStudyCount(safe(restStat.getWorkStudy()))
        .statStartDate(statStartDate)
        .statEndDate(statEndDate)
        .countType(type)
        .build();
  }

  private AdvancedFeeStats getModelUnpaidFeeStats(
      UnpaidFeesStats restStat,
      LocalDate statStartDate,
      LocalDate statEndDate,
      AdvancedFeeStatsCountType type) {
    return AdvancedFeeStats.builder()
        .id(UUID.randomUUID().toString())
        .statType(UNPAID_COUNT)
        .firstGradeCountMonthly(safe(restStat.getFirstGradeMonthly()))
        .secondGradeCountMonthly(safe(restStat.getSecondGradeMonthly()))
        .thirdGradeCountMonthly(safe(restStat.getThirdGradeMonthly()))
        .firstGradeCountYearly(safe(restStat.getFirstGradeYearly()))
        .secondGradeCountYearly(safe(restStat.getSecondGradeYearly()))
        .thirdGradeCountYearly(safe(restStat.getThirdGradeYearly()))
        .unknownGradeCount(safe(restStat.getUnknownGrade()))
        .retakeExamFirstGradeCount(safe(restStat.getRetakeExamFirstGradeCount()))
        .retakeExamSecondGradeCount(safe(restStat.getRetakeExamSecondGradeCount()))
        .retakeExamThirdGradeCount(safe(restStat.getRetakeExamThirdGradeCount()))
        .studentInsuranceFirstGradeCount(safe(restStat.getStudentInsuranceFirstGradeCount()))
        .studentInsuranceSecondGradeCount(safe(restStat.getStudentInsuranceSecondGradeCount()))
        .studentInsuranceThirdGradeCount(safe(restStat.getStudentInsuranceThirdGradeCount()))
        .monthlyCount(safe(restStat.getMonthly()))
        .yearlyCount(safe(restStat.getYearly()))
        .unknownFrequencyCount(safe(restStat.getUnknownFrequency()))
        .workStudyCount(safe(restStat.getWorkStudy()))
        .statStartDate(statStartDate)
        .statEndDate(statEndDate)
        .countType(type)
        .build();
  }

  private AdvancedFeeStats getModelLateFeeStats(
      LateFeesStats restStat,
      LocalDate statStartDate,
      LocalDate statEndDate,
      AdvancedFeeStatsCountType type) {
    return AdvancedFeeStats.builder()
        .id(UUID.randomUUID().toString())
        .statType(LATE_COUNT)
        .firstGradeCountMonthly(safe(restStat.getFirstGradeMonthly()))
        .secondGradeCountMonthly(safe(restStat.getSecondGradeMonthly()))
        .thirdGradeCountMonthly(safe(restStat.getThirdGradeMonthly()))
        .firstGradeCountYearly(safe(restStat.getFirstGradeYearly()))
        .secondGradeCountYearly(safe(restStat.getSecondGradeYearly()))
        .thirdGradeCountYearly(safe(restStat.getThirdGradeYearly()))
        .unknownGradeCount(safe(restStat.getUnknownGrade()))
        .monthlyCount(safe(restStat.getMonthly()))
        .yearlyCount(safe(restStat.getYearly()))
        .unknownFrequencyCount(safe(restStat.getUnknownFrequency()))
        .retakeExamFirstGradeCount(safe(restStat.getRetakeExamFirstGradeCount()))
        .retakeExamSecondGradeCount(safe(restStat.getRetakeExamSecondGradeCount()))
        .retakeExamThirdGradeCount(safe(restStat.getRetakeExamThirdGradeCount()))
        .studentInsuranceFirstGradeCount(safe(restStat.getStudentInsuranceFirstGradeCount()))
        .studentInsuranceSecondGradeCount(safe(restStat.getStudentInsuranceSecondGradeCount()))
        .studentInsuranceThirdGradeCount(safe(restStat.getStudentInsuranceThirdGradeCount()))
        .workStudyCount(safe(restStat.getWorkStudy()))
        .statStartDate(statStartDate)
        .statEndDate(statEndDate)
        .countType(type)
        .build();
  }

  private LateFeesStats getRestLateFeeStats(AdvancedFeeStats modelStat) {
    return new LateFeesStats()
        .firstGradeMonthly(modelStat.getFirstGradeCountMonthly())
        .secondGradeMonthly(modelStat.getSecondGradeCountMonthly())
        .thirdGradeMonthly(modelStat.getThirdGradeCountMonthly())
        .firstGradeYearly(modelStat.getFirstGradeCountYearly())
        .secondGradeYearly(modelStat.getSecondGradeCountYearly())
        .thirdGradeYearly(modelStat.getThirdGradeCountYearly())
        .unknownGrade(modelStat.getUnknownGradeCount())
        .retakeExamFirstGradeCount(modelStat.getRetakeExamFirstGradeCount())
        .retakeExamSecondGradeCount(modelStat.getRetakeExamSecondGradeCount())
        .retakeExamThirdGradeCount(modelStat.getRetakeExamThirdGradeCount())
        .studentInsuranceFirstGradeCount(modelStat.getStudentInsuranceFirstGradeCount())
        .studentInsuranceSecondGradeCount(modelStat.getStudentInsuranceSecondGradeCount())
        .studentInsuranceThirdGradeCount(modelStat.getStudentInsuranceThirdGradeCount())
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount())
        .unknownFrequency(modelStat.getUnknownFrequencyCount());
  }

  private PendingFeesStats getRestPendingFeeStats(AdvancedFeeStats modelStat) {
    return new PendingFeesStats()
        .firstGradeMonthly(modelStat.getFirstGradeCountMonthly())
        .secondGradeMonthly(modelStat.getSecondGradeCountMonthly())
        .thirdGradeMonthly(modelStat.getThirdGradeCountMonthly())
        .firstGradeYearly(modelStat.getFirstGradeCountYearly())
        .secondGradeYearly(modelStat.getSecondGradeCountYearly())
        .thirdGradeYearly(modelStat.getThirdGradeCountYearly())
        .unknownGrade(modelStat.getUnknownGradeCount())
        .retakeExamFirstGradeCount(modelStat.getRetakeExamFirstGradeCount())
        .retakeExamSecondGradeCount(modelStat.getRetakeExamSecondGradeCount())
        .retakeExamThirdGradeCount(modelStat.getRetakeExamThirdGradeCount())
        .studentInsuranceFirstGradeCount(modelStat.getStudentInsuranceFirstGradeCount())
        .studentInsuranceSecondGradeCount(modelStat.getStudentInsuranceSecondGradeCount())
        .studentInsuranceThirdGradeCount(modelStat.getStudentInsuranceThirdGradeCount())
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount())
        .unknownFrequency(modelStat.getUnknownFrequencyCount());
  }

  private UnpaidFeesStats getRestUnpaidFeeStats(AdvancedFeeStats modelStat) {
    return new UnpaidFeesStats()
        .firstGradeMonthly(modelStat.getFirstGradeCountMonthly())
        .secondGradeMonthly(modelStat.getSecondGradeCountMonthly())
        .thirdGradeMonthly(modelStat.getThirdGradeCountMonthly())
        .firstGradeYearly(modelStat.getFirstGradeCountYearly())
        .secondGradeYearly(modelStat.getSecondGradeCountYearly())
        .thirdGradeYearly(modelStat.getThirdGradeCountYearly())
        .unknownGrade(modelStat.getUnknownGradeCount())
        .retakeExamFirstGradeCount(modelStat.getRetakeExamFirstGradeCount())
        .retakeExamSecondGradeCount(modelStat.getRetakeExamSecondGradeCount())
        .retakeExamThirdGradeCount(modelStat.getRetakeExamThirdGradeCount())
        .studentInsuranceFirstGradeCount(modelStat.getStudentInsuranceFirstGradeCount())
        .studentInsuranceSecondGradeCount(modelStat.getStudentInsuranceSecondGradeCount())
        .studentInsuranceThirdGradeCount(modelStat.getStudentInsuranceThirdGradeCount())
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount())
        .unknownFrequency(modelStat.getUnknownFrequencyCount());
  }

  private PaidFeesStats getRestPaidFeeStats(AdvancedFeeStats modelStat) {
    return new PaidFeesStats()
        .firstGradeMonthly(modelStat.getFirstGradeCountMonthly())
        .secondGradeMonthly(modelStat.getSecondGradeCountMonthly())
        .thirdGradeMonthly(modelStat.getThirdGradeCountMonthly())
        .firstGradeYearly(modelStat.getFirstGradeCountYearly())
        .secondGradeYearly(modelStat.getSecondGradeCountYearly())
        .thirdGradeYearly(modelStat.getThirdGradeCountYearly())
        .unknownGrade(modelStat.getUnknownGradeCount())
        .retakeExamFirstGradeCount(modelStat.getRetakeExamFirstGradeCount())
        .retakeExamSecondGradeCount(modelStat.getRetakeExamSecondGradeCount())
        .retakeExamThirdGradeCount(modelStat.getRetakeExamThirdGradeCount())
        .studentInsuranceFirstGradeCount(modelStat.getStudentInsuranceFirstGradeCount())
        .studentInsuranceSecondGradeCount(modelStat.getStudentInsuranceSecondGradeCount())
        .studentInsuranceThirdGradeCount(modelStat.getStudentInsuranceThirdGradeCount())
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount())
        .unknownFrequency(modelStat.getUnknownFrequencyCount())
        .mobileMoney(BigDecimal.valueOf(modelStat.getMpbsCount()))
        .bankFees(BigDecimal.valueOf(modelStat.getBankTransferCount()));
  }

  private TotalExpectedFeesStats getRestTotalFeeStats(AdvancedFeeStats modelStat) {
    return new TotalExpectedFeesStats()
        .firstGradeMonthly(modelStat.getFirstGradeCountMonthly())
        .secondGradeMonthly(modelStat.getSecondGradeCountMonthly())
        .thirdGradeMonthly(modelStat.getThirdGradeCountMonthly())
        .firstGradeYearly(modelStat.getFirstGradeCountYearly())
        .secondGradeYearly(modelStat.getSecondGradeCountYearly())
        .thirdGradeYearly(modelStat.getThirdGradeCountYearly())
        .unknownGrade(modelStat.getUnknownGradeCount())
        .retakeExamFirstGradeCount(modelStat.getRetakeExamFirstGradeCount())
        .retakeExamSecondGradeCount(modelStat.getRetakeExamSecondGradeCount())
        .retakeExamThirdGradeCount(modelStat.getRetakeExamThirdGradeCount())
        .studentInsuranceFirstGradeCount(modelStat.getStudentInsuranceFirstGradeCount())
        .studentInsuranceSecondGradeCount(modelStat.getStudentInsuranceSecondGradeCount())
        .studentInsuranceThirdGradeCount(modelStat.getStudentInsuranceThirdGradeCount())
        .unknownGrade(modelStat.getUnknownGradeCount())
        .workStudy(modelStat.getWorkStudyCount())
        .monthly(modelStat.getMonthlyCount())
        .yearly(modelStat.getYearlyCount())
        .unknownFrequency(modelStat.getUnknownFrequencyCount());
  }

  public AdvancedFeesStatistics toRest(
      Map<AdvancedFeeStatsType, AdvancedFeeStats> modelStat, boolean expired) {
    AdvancedFeesStatistics restStat = new AdvancedFeesStatistics();
    for (Entry<AdvancedFeeStatsType, AdvancedFeeStats> entry : modelStat.entrySet()) {
      switch (entry.getKey()) {
        case PENDING_COUNT -> restStat.pendingFeesCount(getRestPendingFeeStats(entry.getValue()));
        case LATE_COUNT -> restStat.lateFeesCount(getRestLateFeeStats(entry.getValue()));
        case PAID_COUNT -> restStat.paidFeesCount(getRestPaidFeeStats(entry.getValue()));
        case UNPAID_COUNT -> restStat.unpaidFeesCount(getRestUnpaidFeeStats(entry.getValue()));
        case TOTAL_COUNT -> restStat.totalExpectedFeesCount(getRestTotalFeeStats(entry.getValue()));
      }
      restStat.updateDatetime(entry.getValue().getUpdateDatetime());
    }
    restStat.expired(expired);
    return restStat;
  }

  private Long safe(Long value) {
    return value != null ? value : 0L;
  }

  private Long safeBigDecimal(BigDecimal value) {
    return value != null ? value.longValue() : null;
  }
}
