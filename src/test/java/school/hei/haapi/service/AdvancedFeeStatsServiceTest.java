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
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.LATE_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PAID_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.PENDING_COUNT;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsType.TOTAL_COUNT;
import static school.hei.haapi.service.utils.InstantUtils.now;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.mapper.AdvancedFeeStatsMapper;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.statistics.AdvancedFeeStats;
import school.hei.haapi.repository.AdvancedFeeStatsRepository;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.dao.FeeDao;

public class AdvancedFeeStatsServiceTest extends FacadeITMockedThirdParties {
  private AdvancedFeeStatsService subject;
  private FeeDao feeDao;
  private AdvancedFeeStatsRepository repository;
  @Autowired private AdvancedFeeStatsMapper advancedFeeStatsMapper;
  private FeeRepository feeRepository;

  @BeforeEach
  void setUp() {
    feeDao = mock(FeeDao.class);
    repository = mock(AdvancedFeeStatsRepository.class);
    feeRepository = mock(FeeRepository.class);

    subject =
        new AdvancedFeeStatsService(feeDao, repository, advancedFeeStatsMapper, feeRepository);
  }

  @Test
  void advanced_fee_statistics_count_ok() {
    var rangeDate = Optional.of(now());
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
                .build(),
            AdvancedFeeStats.builder()
                .statType(PENDING_COUNT)
                .firstGradeCount(0)
                .secondGradeCount(1)
                .thirdGradeCount(1)
                .workStudyCount(0)
                .yearlyCount(1)
                .monthlyCount(1)
                .build(),
            AdvancedFeeStats.builder()
                .statType(TOTAL_COUNT)
                .firstGradeCount(4)
                .secondGradeCount(3)
                .thirdGradeCount(3)
                .workStudyCount(2)
                .yearlyCount(7)
                .monthlyCount(5)
                .build());

    when(feeRepository.findAllByDueDatetimeBetween(any(), any())).thenReturn(getFeeList());
    when(feeDao.getAdvancedFeeStatsOnDate(any())).thenReturn(Map.of());
    List<AdvancedFeeStats> stats = subject.generateAdvancedFeeStats(rangeDate, rangeDate);

    assertEquals(
        expectedStats,
        stats.stream()
            .peek(
                stat -> {
                  stat.setId(null);
                  stat.setStatDate(null);
                })
            .collect(toSet()));
  }

  private List<Fee> getFeeList() {
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
            .build());
  }
}
