package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType.ACCOUNTING;
import static school.hei.haapi.model.statistics.AdvancedFeeStats.AdvancedFeeStatsCountType.RECEIPT;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeStatusHistory;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.service.AdvancedFeeStatsService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class AdvancedFeeStatsServiceIT extends FacadeITMockedThirdParties {
  private static List<Fee> feeDueJunePaidInJuly;
  private static List<Fee> feeDueJunePaidInMay;
  private static List<Fee> feeDueJunePending;
  private static List<Fee> feeDueJuneUnpaid;
  private static List<Fee> feeDueJuneLate;

  @MockBean private FeeRepository feeRepositoryMock;
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private AdvancedFeeStatsService subject;

  @BeforeAll
  static void initializeMocks() {
    feeDueJunePaidInJuly = List.of(createFeeDueJunePaidJuly());
    feeDueJunePaidInMay = List.of(createFeeDueJunePaidMay());
    feeDueJunePending = List.of(createFeeDueJunePending());
    feeDueJuneUnpaid = List.of(createFeeDueJuneUnpaid());
    feeDueJuneLate = List.of(createFeeDueJuneLate());
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private static Fee createFeeDueJunePaidJuly() {
    List<FeeStatusHistory> statusHistories =
        List.of(
            createStatus(PENDING, "2025-06-27T00:00:00.00Z"),
            createStatus(PAID, "2025-07-17T00:00:00.00Z"));
    return createFee(statusHistories, "2025-06-30T23:59:59Z", PAID);
  }

  private static Fee createFeeDueJunePaidMay() {
    List<FeeStatusHistory> statusHistories =
        List.of(
            createStatus(PENDING, "2025-05-07T00:00:00.00Z"),
            createStatus(PAID, "2025-05-17T00:00:00.00Z"));
    return createFee(statusHistories, "2025-06-30T23:59:59Z", PAID);
  }

  private static FeeStatusHistory createStatus(FeeStatusEnum status, String datetime) {
    return FeeStatusHistory.builder()
        .id(UUID.randomUUID().toString())
        .status(status)
        .datetime(Instant.parse(datetime))
        .build();
  }

  private static Fee createFee(
      List<FeeStatusHistory> statusHistories, String dueDatetime, FeeStatusEnum status) {
    return Fee.builder()
        .id(UUID.randomUUID().toString())
        .statusHistories(statusHistories)
        .category(L1)
        .type(TUITION)
        .frequency(MONTHLY)
        .status(status)
        .mobilePayments(List.of())
        .dueDatetime(Instant.parse(dueDatetime))
        .build();
  }

  private static Fee createFeeDueJunePending() {
    List<FeeStatusHistory> statusHistories =
        List.of(createStatus(PENDING, "2025-06-27T00:00:00.00Z"));
    return createFee(statusHistories, "2025-06-30T23:59:59Z", PENDING);
  }

  private static Fee createFeeDueJuneUnpaid() {
    List<FeeStatusHistory> statusHistories =
        List.of(
            createStatus(PENDING, "2025-06-27T00:00:00.00Z"),
            createStatus(UNPAID, "2025-06-30T23:59:59Z"));
    return createFee(statusHistories, "2025-06-30T23:59:59Z", UNPAID);
  }

  private static Fee createFeeDueJuneLate() {
    List<FeeStatusHistory> statusHistories =
        List.of(
            createStatus(PENDING, "2025-06-27T00:00:00.00Z"),
            createStatus(LATE, "2025-07-01T00:00:00.00Z"));
    return createFee(statusHistories, "2025-06-30T23:59:59Z", LATE);
  }

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void accounting_fee_due_june_paid_july_counts_as_paid_june() {
    when(feeRepositoryMock.findAllByDueDatetimeBetween(any(), any()))
        .thenReturn(feeDueJunePaidInJuly);
    subject.updateAdvancedFeeStats(
        Optional.of(Instant.parse("2025-06-01T00:00:00Z")),
        Optional.of(Instant.parse("2025-06-30T23:59:59Z")),
        Optional.of(ACCOUNTING));
    var generatedJuneStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30), Optional.empty());

    when(feeRepositoryMock.findAllByDueDatetimeBetween(any(), any())).thenReturn(List.of());
    subject.updateAdvancedFeeStats(
        Optional.of(Instant.parse("2025-07-01T00:00:00Z")),
        Optional.of(Instant.parse("2025-07-31T23:59:59Z")),
        Optional.of(ACCOUNTING));
    var generatedJulyStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31), Optional.empty());

    assertEquals(1, generatedJuneStats.getPaidFeesCount().getMonthly());
    assertEquals(0, generatedJulyStats.getPaidFeesCount().getMonthly());
  }

  @Test
  void receipt_fee_due_june_paid_july_counts_as_paid_july() {
    when(feeRepositoryMock.findAllByStatusHistoriesDatetimeBetween(any(), any()))
        .thenReturn(feeDueJunePaidInJuly);
    subject.updateAdvancedFeeStats(
        Optional.of(Instant.parse("2025-06-01T00:00:00Z")),
        Optional.of(Instant.parse("2025-06-30T23:59:59Z")),
        Optional.of(RECEIPT));
    subject.updateAdvancedFeeStats(
        Optional.of(Instant.parse("2025-07-01T00:00:00Z")),
        Optional.of(Instant.parse("2025-07-31T23:59:59Z")),
        Optional.of(RECEIPT));

    var generatedJuneStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30), Optional.of(RECEIPT));
    var generatedJulyStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31), Optional.of(RECEIPT));
    assertEquals(0, generatedJuneStats.getPaidFeesCount().getMonthly());
    assertEquals(1, generatedJulyStats.getPaidFeesCount().getMonthly());
  }

  @Test
  void request_accounting_not_available_trigger_generation() {
    var generatedAugustStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), Optional.empty());

    assertTrue(generatedAugustStats.getExpired());
  }

  @Test
  void accounting_fee_due_june_paid_may_counts_as_paid_june() {
    when(feeRepositoryMock.findAllByDueDatetimeBetween(any(), any()))
        .thenReturn(feeDueJunePaidInMay);
    subject.updateAdvancedFeeStats(
        Optional.of(Instant.parse("2025-06-01T00:00:00Z")),
        Optional.of(Instant.parse("2025-06-30T23:59:59Z")),
        Optional.of(ACCOUNTING));
    when(feeRepositoryMock.findAllByDueDatetimeBetween(any(), any())).thenReturn(List.of());
    subject.updateAdvancedFeeStats(
        Optional.of(Instant.parse("2025-05-01T00:00:00Z")),
        Optional.of(Instant.parse("2025-05-31T23:59:59Z")),
        Optional.of(ACCOUNTING));

    var generatedJuneStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30), Optional.empty());
    var generatedMayStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31), Optional.empty());
    assertEquals(1, generatedJuneStats.getPaidFeesCount().getMonthly());
    assertEquals(0, generatedMayStats.getPaidFeesCount().getMonthly());
  }

  @Test
  void receipt_fee_due_june_paid_may_counts_as_paid_may() {
    when(feeRepositoryMock.findAllByStatusHistoriesDatetimeBetween(any(), any()))
        .thenReturn(feeDueJunePaidInMay);
    subject.updateAdvancedFeeStats(
        Optional.of(Instant.parse("2025-05-01T00:00:00Z")),
        Optional.of(Instant.parse("2025-05-31T23:59:59Z")),
        Optional.of(RECEIPT));
    when(feeRepositoryMock.findAllByStatusHistoriesDatetimeBetween(any(), any()))
        .thenReturn(List.of());
    subject.updateAdvancedFeeStats(
        Optional.of(Instant.parse("2025-06-01T00:00:00Z")),
        Optional.of(Instant.parse("2025-06-30T23:59:59Z")),
        Optional.of(RECEIPT));

    var generatedJuneStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30), Optional.of(RECEIPT));
    var generatedMayStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31), Optional.of(RECEIPT));
    assertEquals(1, generatedMayStats.getPaidFeesCount().getMonthly());
    assertEquals(0, generatedJuneStats.getPaidFeesCount().getMonthly());
  }

  @Test
  void unpaid_or_pending_or_late_fees_do_not_affect_paid_stats() {
    List<Fee> nonPaidFees =
        List.of(
            feeDueJunePending.getFirst(), feeDueJuneUnpaid.getFirst(), feeDueJuneLate.getFirst());
    when(feeRepositoryMock.findAllByDueDatetimeBetween(any(), any())).thenReturn(nonPaidFees);
    subject.updateAdvancedFeeStats(
        Optional.of(Instant.parse("2025-06-01T00:00:00Z")),
        Optional.of(Instant.parse("2025-06-30T23:59:59Z")),
        Optional.of(ACCOUNTING));

    var generatedJuneStats =
        subject.getAdvancedFeeStats(
            LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30), Optional.empty());

    assertEquals(0, generatedJuneStats.getPaidFeesCount().getMonthly());
  }

  @Test
  void manager_get_advanced_fee_statistics_ok() {
    LocalDateTime fromDateTime = LocalDateTime.parse("2025-04-01T00:00:00.00");
    LocalDateTime toDateTime = LocalDateTime.parse("2025-04-30T23:59:59.99");

    var client = anApiClient(MANAGER1_TOKEN);
    var payingApi = new PayingApi(client);

    assertDoesNotThrow(
        () -> payingApi.getAdvancedFeesStats(fromDateTime.toLocalDate(), toDateTime.toLocalDate()));
  }

  @Test
  void manager_get_advanced_fee_statistics_cached_ok() {
    LocalDateTime fromDateTime = LocalDateTime.parse("2024-04-01T00:00:00.00");
    LocalDateTime toDateTime = LocalDateTime.parse("2024-04-30T23:59:59.99");

    var client = anApiClient(MANAGER1_TOKEN);
    var payingApi = new PayingApi(client);

    assertDoesNotThrow(
        () -> payingApi.getAdvancedFeesStats(fromDateTime.toLocalDate(), toDateTime.toLocalDate()));
  }
}
