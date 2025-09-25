package school.hei.haapi.unit;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.unit.MpbsVerificationTest.someMpbs;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsStatusHistory;
import school.hei.haapi.service.FailedMobilePaymentNotification;
import school.hei.haapi.service.MpbsService;
import school.hei.haapi.service.UnverifiedMobilePaymentHandler;

class UnverifiedMobilePaymentHandlerTest {

  MpbsService mpbsServiceMock = mock();
  FailedMobilePaymentNotification failedMobilePaymentNotificationMock = mock();
  UnverifiedMobilePaymentHandler subject =
      new UnverifiedMobilePaymentHandler(mpbsServiceMock, failedMobilePaymentNotificationMock);

  @Test
  void save_failed_mpbs_and_trigger_failed_mobile_payment_notification() {
    var mpbsMock1 = somePendingMpbs(now());
    var mpbsMock2 = somePendingMpbs(now().minus(1, DAYS));
    var mpbsMock3 = somePendingMpbs(now().minus(2, DAYS));
    var mpbsMock4 = somePendingMpbs(now().minus(6, DAYS));
    var mpbsMock5 = somePendingMpbs(now().minus(25, DAYS));

    assertDoesNotThrow(
        () -> subject.accept(List.of(mpbsMock1, mpbsMock2, mpbsMock3, mpbsMock4, mpbsMock5)));

    ArgumentCaptor<List<Mpbs>> listCaptor = ArgumentCaptor.forClass(List.class);
    verify(failedMobilePaymentNotificationMock, times(1)).accept(listCaptor.capture());
    verify(mpbsServiceMock, times(1)).saveAll(listCaptor.capture());
    var captorValues = listCaptor.getAllValues();
    var actualFailedMpbs = captorValues.getFirst();
    var actualVerifiedMpbs = captorValues.getLast();

    assertFalse(actualVerifiedMpbs.isEmpty());
    assertFalse(actualFailedMpbs.isEmpty());
    assertNotNull(actualFailedMpbs.getFirst().getLastVerificationDatetime());
    var lastVerificationDatetime = actualFailedMpbs.getFirst().getLastVerificationDatetime();
    assertEquals(
        List.of(
            expectedFailedMpbs(lastVerificationDatetime, mpbsMock4.getCreationDatetime()),
            expectedFailedMpbs(lastVerificationDatetime, mpbsMock5.getCreationDatetime())),
        actualFailedMpbs);
    assertEquals(5, actualVerifiedMpbs.size());
    assertEquals(2, actualVerifiedMpbs.stream().filter(m -> FAILED.equals(m.getStatus())).count());
    assertEquals(3, actualVerifiedMpbs.stream().filter(m -> PENDING.equals(m.getStatus())).count());
    assertTrue(
        actualVerifiedMpbs.containsAll(
            List.of(
                expectedFailedMpbs(lastVerificationDatetime, mpbsMock4.getCreationDatetime()),
                expectedFailedMpbs(lastVerificationDatetime, mpbsMock5.getCreationDatetime()),
                expectedPendingMpbs(lastVerificationDatetime),
                expectedPendingMpbs(lastVerificationDatetime),
                expectedPendingMpbs(lastVerificationDatetime))));
  }

  @Test
  void retried_mpbs_validity_period_start_with_last_history() {
    var mpbsRetried =
        Mpbs.builder()
            .creationDatetime(now().minus(25, DAYS))
            .status(PENDING)
            .statusHistory(
                List.of(
                    someStatusAt(PENDING, now().minus(1, DAYS)),
                    someStatusAt(FAILED, now().minus(10, DAYS)),
                    someStatusAt(PENDING, now().minus(25, DAYS))))
            .build();

    assertDoesNotThrow(() -> subject.accept(List.of(mpbsRetried)));

    ArgumentCaptor<List<Mpbs>> listCaptor = ArgumentCaptor.forClass(List.class);
    verify(failedMobilePaymentNotificationMock, times(1)).accept(listCaptor.capture());
    verify(mpbsServiceMock, times(1)).saveAll(listCaptor.capture());
    var captorValues = listCaptor.getAllValues();
    var actualFailedMpbs = captorValues.getFirst();
    var actualVerifiedMpbs = captorValues.getLast();

    assertTrue(actualFailedMpbs.isEmpty());
    assertEquals(1, actualVerifiedMpbs.size());
    assertEquals(0, actualVerifiedMpbs.stream().filter(m -> FAILED.equals(m.getStatus())).count());
    assertEquals(1, actualVerifiedMpbs.stream().filter(m -> PENDING.equals(m.getStatus())).count());
  }

  @Test
  void failed_retried_mpbs_validity_period_start_with_last_history() {
    var mpbsFailedRetried =
        Mpbs.builder()
            .creationDatetime(now().minus(25, DAYS))
            .status(PENDING)
            .statusHistory(
                List.of(
                    someStatusAt(PENDING, now().minus(6, DAYS)),
                    someStatusAt(FAILED, now().minus(10, DAYS)),
                    someStatusAt(PENDING, now().minus(25, DAYS))))
            .build();

    assertDoesNotThrow(() -> subject.accept(List.of(mpbsFailedRetried)));

    ArgumentCaptor<List<Mpbs>> listCaptor = ArgumentCaptor.forClass(List.class);
    verify(failedMobilePaymentNotificationMock, times(1)).accept(listCaptor.capture());
    verify(mpbsServiceMock, times(1)).saveAll(listCaptor.capture());
    var captorValues = listCaptor.getAllValues();
    var actualFailedMpbs = captorValues.getFirst();
    var actualVerifiedMpbs = captorValues.getLast();

    assertFalse(actualFailedMpbs.isEmpty());
    assertFalse(actualVerifiedMpbs.isEmpty());
    assertEquals(1, actualFailedMpbs.size());
    assertEquals(1, actualVerifiedMpbs.size());
    assertEquals(FAILED, actualFailedMpbs.getFirst().getStatus());
    assertEquals(actualFailedMpbs, actualVerifiedMpbs);
  }

  private static Mpbs expectedFailedMpbs(
      Instant lastVerificationDatetime, Instant creationDatetime) {
    return Mpbs.builder()
        .status(FAILED)
        .lastVerificationDatetime(lastVerificationDatetime)
        .creationDatetime(creationDatetime)
        .build();
  }

  private static Mpbs expectedPendingMpbs(Instant lastVerificationDatetime) {
    return Mpbs.builder()
        .status(PENDING)
        .lastVerificationDatetime(lastVerificationDatetime)
        .build();
  }

  private static MpbsStatusHistory someStatusAt(MpbsStatus status, Instant statusInstant) {
    return MpbsStatusHistory.builder().creationInstant(statusInstant).status(status).build();
  }

  private static Mpbs somePendingMpbs(Instant creationDateTime) {
    return someMpbs(null, creationDateTime, null).toBuilder()
        .status(PENDING)
        .amount(null)
        .fee(null)
        .build();
  }
}
