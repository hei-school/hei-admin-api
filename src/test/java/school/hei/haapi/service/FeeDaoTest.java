package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.LATE_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PAID_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PENDING_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.TOTAL_COUNT;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.statistics.AdvancedFeeStats;
import school.hei.haapi.repository.dao.FeeDao;

class FeeDaoTest extends FacadeITMockedThirdParties {
  @Autowired FeeDao subject;

  @Test
  @Disabled("TODO: fix advanced fee WIP")
  void get_advanced_fees_stats_ok() {
    LocalDateTime fromDateTime = LocalDateTime.parse("2025-04-01T00:00:00.00");
    LocalDateTime toDateTime = LocalDateTime.parse("2025-04-30T23:59:59.99");

    Map<AdvancedFeeStats.AdvancedFeeStatsType, AdvancedFeeStats> actual =
        subject.getAdvancedFeeStats(fromDateTime.toLocalDate(), toDateTime.toLocalDate());
    Set<AdvancedFeeStats> expectedStats =
        Set.of(
            AdvancedFeeStats.builder()
                .statType(LATE_COUNT)
                .firstGradeCount(0)
                .secondGradeCount(0)
                .thirdGradeCount(0)
                .workStudyCount(0)
                .yearlyCount(0)
                .monthlyCount(0)
                .build(),
            AdvancedFeeStats.builder()
                .statType(PAID_COUNT)
                .firstGradeCount(186)
                .secondGradeCount(118)
                .thirdGradeCount(84)
                .workStudyCount(20)
                .remedialFeesCount(14)
                .unknownGradeCount(0)
                .yearlyCount(2)
                .monthlyCount(388)
                .unknownFrequencyCount(18)
                .bankTransferCount(3L)
                .mpbsCount(405L)
                .build(),
            AdvancedFeeStats.builder()
                .statType(PENDING_COUNT)
                .firstGradeCount(0)
                .secondGradeCount(0)
                .thirdGradeCount(0)
                .workStudyCount(0)
                .yearlyCount(0)
                .monthlyCount(0)
                .build(),
            AdvancedFeeStats.builder()
                .statType(TOTAL_COUNT)
                .firstGradeCount(194)
                .secondGradeCount(118)
                .thirdGradeCount(84)
                .workStudyCount(22)
                .remedialFeesCount(0)
                .yearlyCount(2)
                .monthlyCount(396)
                .unknownFrequencyCount(20)
                .build());

    assertEquals(expectedStats, new HashSet<>(actual.values()));
  }
}
