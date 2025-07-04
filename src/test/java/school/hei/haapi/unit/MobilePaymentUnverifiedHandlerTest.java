package school.hei.haapi.unit;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.service.FailedMobilePaymentNotification;
import school.hei.haapi.service.MobilePaymentUnverifiedHandler;
import school.hei.haapi.service.MpbsService;

class MobilePaymentUnverifiedHandlerTest {

  MpbsService mpbsServiceMock = mock();
  FailedMobilePaymentNotification failedMobilePaymentNotificationMock = mock();
  MobilePaymentUnverifiedHandler subject =
      new MobilePaymentUnverifiedHandler(mpbsServiceMock, failedMobilePaymentNotificationMock);

  @Test
  void save_failed_mpbs_and_trigger_failed_mobile_payment_notification() {
    var mpbsMock1 = mock(Mpbs.class);
    var mpbsMock2 = mock(Mpbs.class);
    var mpbsMock3 = mock(Mpbs.class);
    var mpbsMock4 = mock(Mpbs.class);
    var mpbsMock5 = mock(Mpbs.class);

    when(mpbsMock1.getCreationDatetime()).thenReturn(now());
    when(mpbsMock2.getCreationDatetime()).thenReturn(now().minus(1L, DAYS));
    when(mpbsMock3.getCreationDatetime()).thenReturn(now().minus(2L, DAYS));
    when(mpbsMock4.getCreationDatetime()).thenReturn(now().minus(3L, DAYS));
    when(mpbsMock5.getCreationDatetime()).thenReturn(now().minus(25L, DAYS));
    when(mpbsMock1.getStatus()).thenReturn(PENDING);
    when(mpbsMock2.getStatus()).thenReturn(PENDING);
    when(mpbsMock3.getStatus()).thenReturn(PENDING);
    when(mpbsMock4.getStatus()).thenReturn(PENDING);
    when(mpbsMock5.getStatus()).thenReturn(PENDING);
    Mpbs.MpbsBuilder mpbsBuilder = new Mpbs().toBuilder();
    when(mpbsMock1.toBuilder()).thenReturn(mpbsBuilder);
    when(mpbsMock2.toBuilder()).thenReturn(mpbsBuilder);
    when(mpbsMock3.toBuilder()).thenReturn(mpbsBuilder);
    when(mpbsMock4.toBuilder()).thenReturn(mpbsBuilder);
    when(mpbsMock5.toBuilder()).thenReturn(mpbsBuilder);

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
