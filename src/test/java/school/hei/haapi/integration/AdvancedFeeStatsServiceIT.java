package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType.RECEIPT;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeStatusHistory;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.service.AdvancedFeeStatsService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class AdvancedFeeStatsServiceIT extends FacadeITMockedThirdParties {
  @MockBean private FeeRepository feeRepositoryMock;
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private AdvancedFeeStatsService subject;

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  void receipt_generated_fee_late_june_paid_july_stats_paid_june() {
    List<FeeStatusHistory> mockedStatusHistory =
        List.of(
            FeeStatusHistory.builder()
                .id("1")
                .status(PENDING)
                .datetime(Instant.parse("2025-06-27T00:00:00.00Z"))
                .build(),
            FeeStatusHistory.builder()
                .id("2")
                .status(PAID)
                .datetime(Instant.parse("2025-07-17T00:00:00.00Z"))
                .build());

    List<Fee> mockedFees =
        List.of(
            Fee.builder()
                .id("1")
                .category(L1)
                .type(TUITION)
                .status(PAID)
                .frequency(MONTHLY)
                .mobilePayments(List.of())
                .statusHistories(mockedStatusHistory)
                .dueDatetime(Instant.parse("2025-06-30T00:00:00.00Z"))
                .build());
    when(feeRepositoryMock.findAllByDueDatetimeBetween(any(), any())).thenReturn(mockedFees);
    subject.updateAdvancedFeeStats(
        Optional.of(Instant.parse("2025-06-01T00:00:00Z")),
        Optional.of(Instant.parse("2025-06-30T23:59:59Z")),
        Optional.of(RECEIPT));

    var generatedJuneStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30), Optional.empty());

    when(feeRepositoryMock.findAllByDueDatetimeBetween(any(), any())).thenReturn(List.of());
    subject.updateAdvancedFeeStats(
        Optional.of(Instant.parse("2025-07-01T00:00:00Z")),
        Optional.of(Instant.parse("2025-07-31T23:59:59Z")),
        Optional.of(RECEIPT));
    var generatedJulyStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31), Optional.empty());

    assertEquals(1, generatedJuneStats.getPaidFeesCount().getMonthly());
    assertEquals(0, generatedJulyStats.getPaidFeesCount().getMonthly());
  }

  @Test
  void request_receipt_not_avaialble_trigger_generation() {
    var generatedAugustStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31), Optional.empty());

    assertTrue(generatedAugustStats.getExpired());
  }
}
