package school.hei.haapi.service;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L2;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L3;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.WORK_FEES;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.YEARLY;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType.RECEIPT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.LATE_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PAID_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PENDING_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.TOTAL_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.UNPAID_COUNT;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.rest.mapper.AdvancedFeeStatsMapper;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeStatusHistory;
import school.hei.haapi.model.statistics.AdvancedFeeStats;
import school.hei.haapi.repository.AdvancedFeeStatsRepository;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.dao.FeeDao;

class AdvancedFeeStatsServiceTest extends FacadeITMockedThirdParties {
  private AdvancedFeeStatsService subject;
  private FeeDao feeDao;
  private EventProducer eventProducer;
  private AdvancedFeeStatsRepository repository;
  @Autowired private AdvancedFeeStatsMapper advancedFeeStatsMapper;
  private FeeRepository feeRepository;

  @BeforeEach
  void setUp() {
    feeDao = mock(FeeDao.class);
    repository = mock(AdvancedFeeStatsRepository.class);
    feeRepository = mock(FeeRepository.class);
    eventProducer = mock(EventProducer.class);

    subject =
        new AdvancedFeeStatsService(
            feeDao, repository, advancedFeeStatsMapper, feeRepository, eventProducer);
  }

  @Test
  void getAdvancedFeeStats_withEmptyData_is_expired_ok() {
    var from = LocalDate.of(2025, 6, 1);
    var to = LocalDate.of(2025, 6, 30);
    when(feeDao.getAdvancedFeeStatsOnDateBetween(any(), any(), any())).thenReturn(Map.of());
    var advancedFeeStats = subject.getAdvancedFeeStats(from, to, Optional.of(RECEIPT));

    assertEquals(Boolean.TRUE, advancedFeeStats.getExpired());
  }

  @Test
  void getAdvancedFeeStats_shouldUpdateData_ok() {
    var from = LocalDate.of(2025, 6, 1);
    var to = LocalDate.of(2025, 6, 30);
    var oldAdvancedFeeStats =
        AdvancedFeeStats.builder()
            .id("stats-id-1")
            .updateDatetime(Instant.parse("2020-06-01T00:00:00.00Z"))
            .build();
    Map<AdvancedFeeStats.AdvancedFeeStatsType, AdvancedFeeStats> oldStatsMap = new HashMap<>();
    oldStatsMap.put(PENDING_COUNT, oldAdvancedFeeStats);
    when(feeDao.getAdvancedFeeStatsOnDateBetween(from, to, RECEIPT)).thenReturn(oldStatsMap);
    var result = subject.getAdvancedFeeStats(from, to, Optional.of(RECEIPT));
    assertEquals(Boolean.TRUE, result.getExpired());
  }

  @Test
  void advanced_fee_statistics_count_ok() {
    var rangeDate = Optional.of(Instant.now());
    Set<AdvancedFeeStats> expectedStats =
        Set.of(
            AdvancedFeeStats.builder()
                .statType(LATE_COUNT)
                .firstGradeCount(1)
                .secondGradeCount(1)
                .thirdGradeCount(1)
                .workStudyCount(1)
                .yearlyCount(2)
                .monthlyCount(2)
                .countType(RECEIPT)
                .build(),
            AdvancedFeeStats.builder()
                .statType(PAID_COUNT)
                .firstGradeCount(3)
                .secondGradeCount(1)
                .thirdGradeCount(1)
                .workStudyCount(1)
                .yearlyCount(4)
                .monthlyCount(2)
                .bankTransferCount(6L)
                .mpbsCount(0L)
                .countType(RECEIPT)
                .build(),
            AdvancedFeeStats.builder()
                .statType(PENDING_COUNT)
                .firstGradeCount(0)
                .secondGradeCount(1)
                .thirdGradeCount(1)
                .workStudyCount(0)
                .yearlyCount(1)
                .monthlyCount(1)
                .countType(RECEIPT)
                .build(),
            AdvancedFeeStats.builder()
                .statType(UNPAID_COUNT)
                .firstGradeCount(0)
                .secondGradeCount(2)
                .thirdGradeCount(0)
                .workStudyCount(0)
                .yearlyCount(0)
                .monthlyCount(2)
                .countType(RECEIPT)
                .build(),
            AdvancedFeeStats.builder()
                .statType(TOTAL_COUNT)
                .firstGradeCount(4)
                .secondGradeCount(5)
                .thirdGradeCount(3)
                .workStudyCount(2)
                .yearlyCount(7)
                .monthlyCount(7)
                .countType(RECEIPT)
                .build());

    when(feeRepository.findDistinctByStatusHistoriesDatetimeBetween(any(), any()))
        .thenReturn(getFeeList());
    when(feeDao.getAdvancedFeeStatsOnDateBetween(any(), any(), any())).thenReturn(Map.of());
    List<AdvancedFeeStats> stats = subject.generateAdvancedFeeStats(rangeDate, rangeDate, RECEIPT);
    Set<AdvancedFeeStats> actualStats =
        stats.stream()
            .peek(
                stat -> {
                  stat.setId(null);
                  stat.setStatStartDate(null);
                  stat.setStatEndDate(null);
                })
            .collect(toSet());
    AdvancedFeeStats unpaidStat =
        actualStats.stream()
            .filter(s -> s.getStatType() == UNPAID_COUNT)
            .findFirst()
            .orElseThrow(() -> new AssertionError("UNPAID_COUNT stat not generated"));

    assertEquals(2, unpaidStat.getSecondGradeCount(), "L2 unpaid");
    assertEquals(2, unpaidStat.getMonthlyCount()); // both are MONTHLY
    assertEquals(expectedStats, actualStats);
  }

  private List<Fee> getFeeList() {
    var paidStatusHistory =
        List.of(
            FeeStatusHistory.builder()
                .status(PAID)
                .datetime(Instant.parse("2021-04-11T00:00:00.00Z"))
                .build());
    var lateStatusHistory =
        List.of(
            FeeStatusHistory.builder()
                .status(LATE)
                .datetime(Instant.parse("2021-04-11T00:00:00.00Z"))
                .build());
    var pendingStatusHistory =
        List.of(
            FeeStatusHistory.builder()
                .status(PENDING)
                .datetime(Instant.parse("2021-04-11T00:00:00.00Z"))
                .build());
    var unpaidStatusHistory =
        List.of(
            FeeStatusHistory.builder()
                .status(UNPAID)
                .datetime(Instant.parse("2021-04-11T00:00:00.00Z"))
                .build());
    return List.of(
        Fee.builder()
            .id("1")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(L1)
            .status(PAID)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-04-30T00:00:00.00Z"))
            .frequency(MONTHLY)
            .type(TUITION)
            .statusHistories(paidStatusHistory)
            .build(),
        Fee.builder()
            .id("2")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(L3)
            .status(PAID)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-04-30T00:00:00.00Z"))
            .frequency(YEARLY)
            .type(TUITION)
            .statusHistories(paidStatusHistory)
            .build(),
        Fee.builder()
            .id("3")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(L1)
            .status(PAID)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-04-30T00:00:00.00Z"))
            .frequency(MONTHLY)
            .type(TUITION)
            .statusHistories(paidStatusHistory)
            .build(),
        Fee.builder()
            .id("4")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(L2)
            .status(LATE)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-05-31T00:00:00.00Z"))
            .frequency(YEARLY)
            .type(TUITION)
            .statusHistories(lateStatusHistory)
            .build(),
        Fee.builder()
            .id("5")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(L1)
            .status(LATE)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-05-31T00:00:00.00Z"))
            .frequency(MONTHLY)
            .type(TUITION)
            .statusHistories(lateStatusHistory)
            .build(),
        Fee.builder()
            .id("6")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(L3)
            .status(PENDING)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-06-30T00:00:00.00Z"))
            .frequency(YEARLY)
            .type(TUITION)
            .statusHistories(pendingStatusHistory)
            .build(),
        Fee.builder()
            .id("7")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(L2)
            .status(PENDING)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-07-31T00:00:00.00Z"))
            .frequency(MONTHLY)
            .type(TUITION)
            .statusHistories(pendingStatusHistory)
            .build(),
        Fee.builder()
            .id("8")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(L1)
            .status(PAID)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-08-31T00:00:00.00Z"))
            .frequency(YEARLY)
            .type(TUITION)
            .statusHistories(paidStatusHistory)
            .build(),
        Fee.builder()
            .id("9")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(L3)
            .status(LATE)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-09-30T00:00:00.00Z"))
            .frequency(MONTHLY)
            .type(TUITION)
            .statusHistories(lateStatusHistory)
            .build(),
        Fee.builder()
            .id("10")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(L2)
            .status(PAID)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-10-31T00:00:00.00Z"))
            .frequency(YEARLY)
            .type(TUITION)
            .statusHistories(paidStatusHistory)
            .build(),
        Fee.builder()
            .id("11")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(WORK_FEES)
            .status(LATE)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-10-31T00:00:00.00Z"))
            .frequency(YEARLY)
            .type(TUITION)
            .statusHistories(lateStatusHistory)
            .build(),
        Fee.builder()
            .id("12")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(WORK_FEES)
            .status(PAID)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-10-31T00:00:00.00Z"))
            .frequency(YEARLY)
            .type(TUITION)
            .statusHistories(paidStatusHistory)
            .build(),
        Fee.builder()
            .id("14")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(L2)
            .status(UNPAID)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-07-31T00:00:00.00Z"))
            .frequency(MONTHLY)
            .type(TUITION)
            .statusHistories(unpaidStatusHistory)
            .build(),
        Fee.builder()
            .id("15")
            .creationDatetime(Instant.parse("2025-04-11T00:00:00.00Z"))
            .category(L2)
            .status(UNPAID)
            .mobilePayments(List.of())
            .dueDatetime(Instant.parse("2025-07-31T00:00:00.00Z"))
            .frequency(MONTHLY)
            .type(TUITION)
            .statusHistories(unpaidStatusHistory)
            .build());
  }
}
