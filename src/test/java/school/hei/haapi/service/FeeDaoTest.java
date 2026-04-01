package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType.RECEIPT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.LATE_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PAID_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PENDING_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.TOTAL_COUNT;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.statistics.AdvancedFeeStats;
import school.hei.haapi.repository.dao.FeeDao;

@Slf4j
class FeeDaoTest extends FacadeITMockedThirdParties {
  @Autowired FeeDao subject;

  @Test
  void get_advanced_fees_stats_ok() {
    LocalDateTime fromDateTime = LocalDateTime.parse("2024-04-01T00:00:00.00");
    LocalDateTime toDateTime = LocalDateTime.parse("2024-04-30T23:59:59.99");

    Map<AdvancedFeeStats.AdvancedFeeStatsType, AdvancedFeeStats> stats =
        subject.getAdvancedFeeStatsOnDateBetween(
            fromDateTime.toLocalDate(), toDateTime.toLocalDate(), RECEIPT);
    Set<AdvancedFeeStats> expectedStats =
        Set.of(
            AdvancedFeeStats.builder()
                .id(null) // keep null if ignored in equals
                .statType(LATE_COUNT)
                .firstGradeCountMonthly(0)
                .secondGradeCountMonthly(0)
                .thirdGradeCountMonthly(0)
                .firstGradeCountYearly(0)
                .secondGradeCountYearly(0)
                .thirdGradeCountYearly(0)
                .unknownGradeCount(0)
                .remedialFirstGradeCount(0)
                .remedialSecondGradeCount(0)
                .remedialThirdGradeCount(0)
                .workStudyCount(0)
                .monthlyCount(0)
                .yearlyCount(0)
                .unknownFrequencyCount(0)
                .bankTransferCount(null)
                .mpbsCount(null)
                .creationDatetime(null)
                .updateDatetime(null)
                .statStartDate(null)
                .statEndDate(null)
                .countType(RECEIPT)
                .build(),
            AdvancedFeeStats.builder()
                .id(null)
                .statType(PENDING_COUNT)
                .firstGradeCountMonthly(0)
                .secondGradeCountMonthly(0)
                .thirdGradeCountMonthly(0)
                .firstGradeCountYearly(0)
                .secondGradeCountYearly(0)
                .thirdGradeCountYearly(0)
                .unknownGradeCount(0)
                .remedialFirstGradeCount(0)
                .remedialSecondGradeCount(0)
                .remedialThirdGradeCount(0)
                .workStudyCount(1)
                .monthlyCount(0)
                .yearlyCount(0)
                .unknownFrequencyCount(1)
                .bankTransferCount(null)
                .mpbsCount(null)
                .creationDatetime(null)
                .updateDatetime(null)
                .statStartDate(null)
                .statEndDate(null)
                .countType(RECEIPT)
                .build(),
            AdvancedFeeStats.builder()
                .id(null)
                .statType(TOTAL_COUNT)
                .firstGradeCountMonthly(194)
                .secondGradeCountMonthly(118)
                .thirdGradeCountMonthly(84)
                .firstGradeCountYearly(0)
                .secondGradeCountYearly(0)
                .thirdGradeCountYearly(0)
                .unknownGradeCount(0)
                .remedialFirstGradeCount(0)
                .remedialSecondGradeCount(0)
                .remedialThirdGradeCount(0)
                .workStudyCount(22)
                .monthlyCount(396)
                .yearlyCount(2)
                .unknownFrequencyCount(20)
                .bankTransferCount(null)
                .mpbsCount(null)
                .creationDatetime(null)
                .updateDatetime(null)
                .statStartDate(null)
                .statEndDate(null)
                .countType(RECEIPT)
                .build(),
            AdvancedFeeStats.builder()
                .id(null)
                .statType(PAID_COUNT)
                .firstGradeCountMonthly(186)
                .secondGradeCountMonthly(118)
                .thirdGradeCountMonthly(84)
                .firstGradeCountYearly(0)
                .secondGradeCountYearly(0)
                .thirdGradeCountYearly(14)
                .unknownGradeCount(0)
                .remedialFirstGradeCount(0)
                .remedialSecondGradeCount(0)
                .remedialThirdGradeCount(0)
                .workStudyCount(20)
                .monthlyCount(388)
                .yearlyCount(2)
                .unknownFrequencyCount(18)
                .bankTransferCount(3L)
                .mpbsCount(405L)
                .creationDatetime(null)
                .updateDatetime(null)
                .statStartDate(null)
                .statEndDate(null)
                .countType(RECEIPT)
                .build());
    var values = stats.values();
    log.info("values : " + values);
    var actual =
        values.stream()
            .peek(
                stat -> {
                  stat.setId(null);
                  stat.setStatEndDate(null);
                  stat.setStatStartDate(null);
                  stat.setUpdateDatetime(null);
                  stat.setCreationDatetime(null);
                })
            .toList();
    assertEquals(expectedStats, new HashSet<>(actual));
  }
}
