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

    when(mpbsMock1.getCreationDatetime()).thenReturn(now().minus(1L, DAYS));
    when(mpbsMock2.getCreationDatetime()).thenReturn(now().minus(3L, DAYS));
    when(mpbsMock1.getStatus()).thenReturn(PENDING);
    when(mpbsMock2.getStatus()).thenReturn(PENDING);
    when(mpbsMock1.toBuilder()).thenReturn(new Mpbs().toBuilder());
    when(mpbsMock2.toBuilder()).thenReturn(new Mpbs().toBuilder());

    assertDoesNotThrow(() -> subject.accept(List.of(mpbsMock1, mpbsMock2)));

    var listCaptor = ArgumentCaptor.forClass(List.class);
    verify(failedMobilePaymentNotificationMock, times(1)).accept(listCaptor.capture());
    verify(mpbsServiceMock, times(1)).saveAll(listCaptor.capture());
    List<Mpbs> actualFailedMpbs = (List<Mpbs>) listCaptor.getAllValues().getFirst();
    List<Mpbs> actualVerifiedMpbs = (List<Mpbs>) listCaptor.getAllValues().getLast();

    assertFalse(actualVerifiedMpbs.isEmpty());
    assertFalse(actualFailedMpbs.isEmpty());
    assertNotNull(actualFailedMpbs.getFirst().getLastVerificationDatetime());
    var lastVerificationDatetime = actualFailedMpbs.getFirst().getLastVerificationDatetime();
    assertEquals(List.of(expectedFailedMpbs(lastVerificationDatetime)), actualFailedMpbs);
    assertEquals(2, actualVerifiedMpbs.size());
    assertTrue(
        actualVerifiedMpbs.containsAll(
            List.of(
                expectedFailedMpbs(lastVerificationDatetime),
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
