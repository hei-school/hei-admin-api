package school.hei.haapi.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType.ACCOUNTING;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType.RECEIPT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.LATE_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PAID_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PENDING_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.TOTAL_COUNT;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.statistics.AdvancedFeeStats;
import school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType;
import school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType;
import school.hei.haapi.repository.AdvancedFeeStatsRepository;
import school.hei.haapi.repository.dao.FeeDao;

/**
 * The dao does not compute anything: it reads back the rows already stored for a window. So the
 * test writes the window it then reads, plus neighbours it must not pick up.
 */
@Slf4j
class FeeDaoTest extends FacadeITMockedThirdParties {
  private static final LocalDate FROM = LocalDate.parse("2027-04-01");
  private static final LocalDate TO = LocalDate.parse("2027-04-30");

  @Autowired FeeDao subject;
  @Autowired AdvancedFeeStatsRepository advancedFeeStatsRepository;

  private List<AdvancedFeeStats> ownedStats;

  @BeforeEach
  void setUp() {
    ownedStats =
        advancedFeeStatsRepository.saveAll(
            List.of(
                aStat(LATE_COUNT, RECEIPT, FROM, TO, 0, 0),
                aStat(PENDING_COUNT, RECEIPT, FROM, TO, 0, 1),
                aStat(PAID_COUNT, RECEIPT, FROM, TO, 186, 388),
                aStat(TOTAL_COUNT, RECEIPT, FROM, TO, 194, 396),
                // neighbours the filter has to leave out
                aStat(PAID_COUNT, ACCOUNTING, FROM, TO, 1, 1),
                aStat(PAID_COUNT, RECEIPT, TO.plusDays(1), TO.plusMonths(1), 1, 1)));
  }

  @AfterEach
  void tearDown() {
    advancedFeeStatsRepository.deleteAll(ownedStats);
  }

  private static AdvancedFeeStats aStat(
      AdvancedFeeStatsType statType,
      AdvancedFeeStatsCountType countType,
      LocalDate startDate,
      LocalDate endDate,
      long firstGradeCountMonthly,
      long monthlyCount) {
    return AdvancedFeeStats.builder()
        .id(randomUUID().toString())
        .statType(statType)
        .countType(countType)
        .statStartDate(startDate)
        .statEndDate(endDate)
        .firstGradeCountMonthly(firstGradeCountMonthly)
        .monthlyCount(monthlyCount)
        .build();
  }

  @Test
  void get_advanced_fees_stats_ok() {
    var stats = subject.getAdvancedFeeStatsOnDateBetween(FROM, TO, RECEIPT);

    assertEquals(Set.of(LATE_COUNT, PENDING_COUNT, PAID_COUNT, TOTAL_COUNT), stats.keySet());

    var actual = new HashSet<>(stats.values().stream().map(FeeDaoTest::comparable).toList());
    var expected =
        new HashSet<>(
            ownedStats.stream()
                .filter(stat -> RECEIPT.equals(stat.getCountType()))
                .filter(stat -> TO.equals(stat.getStatEndDate()))
                .map(FeeDaoTest::comparable)
                .toList());

    assertEquals(expected, actual);
  }

  /** Timestamps are database-assigned, so they are compared out. */
  private static String comparable(AdvancedFeeStats stat) {
    return "%s/%s/%s..%s/%d/%d"
        .formatted(
            stat.getStatType(),
            stat.getCountType(),
            stat.getStatStartDate(),
            stat.getStatEndDate(),
            stat.getFirstGradeCountMonthly(),
            stat.getMonthlyCount());
  }
}
