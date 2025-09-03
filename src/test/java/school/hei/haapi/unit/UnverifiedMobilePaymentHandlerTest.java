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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Mpbs.MpbsStatusHistory;
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
    var mpbsMock1 = Mpbs.builder().creationDatetime(now()).status(PENDING).build();
    var mpbsMock2 = Mpbs.builder().creationDatetime(now().minus(1, DAYS)).status(PENDING).build();
    var mpbsMock3 = Mpbs.builder().creationDatetime(now().minus(2, DAYS)).status(PENDING).build();
    var mpbsMock4 = Mpbs.builder().creationDatetime(now().minus(3, DAYS)).status(PENDING).build();
    var mpbsMock5 = Mpbs.builder().creationDatetime(now().minus(25, DAYS)).status(PENDING).build();

    assertDoesNotThrow(
        () -> subject.accept(List.of(mpbsMock1, mpbsMock2, mpbsMock3, mpbsMock4, mpbsMock5)));

    var listCaptor = ArgumentCaptor.forClass(List.class);
    verify(failedMobilePaymentNotificationMock, times(1)).accept(listCaptor.capture());
    verify(mpbsServiceMock, times(1)).saveAll(listCaptor.capture());
    List<Mpbs> actualFailedMpbs = (List<Mpbs>) listCaptor.getAllValues().getFirst();
    List<Mpbs> actualVerifiedMpbs = (List<Mpbs>) listCaptor.getAllValues().getLast();

    assertFalse(actualVerifiedMpbs.isEmpty());
    assertFalse(actualFailedMpbs.isEmpty());
    assertNotNull(actualFailedMpbs.getFirst().getLastVerificationDatetime());
    var lastVerificationDatetime = actualFailedMpbs.getFirst().getLastVerificationDatetime();
    assertEquals(
        List.of(
            expectedFailedMpbs(lastVerificationDatetime),
            expectedFailedMpbs(lastVerificationDatetime)),
        actualFailedMpbs);
    assertEquals(5, actualVerifiedMpbs.size());
    assertEquals(2, actualVerifiedMpbs.stream().filter(m -> FAILED.equals(m.getStatus())).count());
    assertEquals(3, actualVerifiedMpbs.stream().filter(m -> PENDING.equals(m.getStatus())).count());
    assertTrue(
        actualVerifiedMpbs.containsAll(
            List.of(
                expectedFailedMpbs(lastVerificationDatetime),
                expectedFailedMpbs(lastVerificationDatetime),
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
                new ArrayList<>(
                    List.of(
                        MpbsStatusHistory.builder()
                            .creationInstant(now().minus(1, DAYS))
                            .status(PENDING)
                            .build(),
                        MpbsStatusHistory.builder()
                            .creationInstant(now().minus(10, DAYS))
                            .status(FAILED)
                            .build(),
                        MpbsStatusHistory.builder()
                            .creationInstant(now().minus(25, DAYS))
                            .status(PENDING)
                            .build())))
            .build();

    assertDoesNotThrow(() -> subject.accept(List.of(mpbsRetried)));

    var listCaptor = ArgumentCaptor.forClass(List.class);
    verify(failedMobilePaymentNotificationMock, times(1)).accept(listCaptor.capture());
    verify(mpbsServiceMock, times(1)).saveAll(listCaptor.capture());
    List<Mpbs> actualFailedMpbs = (List<Mpbs>) listCaptor.getAllValues().getFirst();
    List<Mpbs> actualVerifiedMpbs = (List<Mpbs>) listCaptor.getAllValues().getLast();

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
                new ArrayList<>(
                    List.of(
                        MpbsStatusHistory.builder()
                            .creationInstant(now().minus(4, DAYS))
                            .status(PENDING)
                            .build(),
                        MpbsStatusHistory.builder()
                            .creationInstant(now().minus(10, DAYS))
                            .status(FAILED)
                            .build(),
                        MpbsStatusHistory.builder()
                            .creationInstant(now().minus(25, DAYS))
                            .status(PENDING)
                            .build())))
            .build();

    assertDoesNotThrow(() -> subject.accept(List.of(mpbsFailedRetried)));

    var listCaptor = ArgumentCaptor.forClass(List.class);
    verify(failedMobilePaymentNotificationMock, times(1)).accept(listCaptor.capture());
    verify(mpbsServiceMock, times(1)).saveAll(listCaptor.capture());
    List<Mpbs> actualFailedMpbs = (List<Mpbs>) listCaptor.getAllValues().getFirst();
    List<Mpbs> actualVerifiedMpbs = (List<Mpbs>) listCaptor.getAllValues().getLast();

    assertFalse(actualFailedMpbs.isEmpty());
    assertFalse(actualVerifiedMpbs.isEmpty());
    assertEquals(1, actualFailedMpbs.size());
    assertEquals(1, actualVerifiedMpbs.size());
    assertEquals(FAILED, actualFailedMpbs.getFirst().getStatus());
    assertEquals(actualFailedMpbs, actualVerifiedMpbs);
  }

  private Mpbs expectedFailedMpbs(Instant lastVerificationDatetime) {
    return Mpbs.builder().status(FAILED).lastVerificationDatetime(lastVerificationDatetime).build();
  }

  private Mpbs expectedPendingMpbs(Instant lastVerificationDatetime) {
    return Mpbs.builder()
        .status(PENDING)
        .lastVerificationDatetime(lastVerificationDatetime)
        .build();
  }
}
