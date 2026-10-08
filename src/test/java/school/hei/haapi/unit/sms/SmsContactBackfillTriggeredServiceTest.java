package school.hei.haapi.unit.sms;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.SmsContactBackfillTriggered;
import school.hei.haapi.service.event.SmsContactBackfillTriggeredService;
import school.hei.haapi.service.sms.SmsContactService;

class SmsContactBackfillTriggeredServiceTest {
  private final SmsContactService smsContactServiceMock = mock();

  private final SmsContactBackfillTriggeredService subject =
      new SmsContactBackfillTriggeredService(smsContactServiceMock);

  @Test
  void delegates_to_the_sms_contact_service_backfill() {
    subject.accept(new SmsContactBackfillTriggered());

    verify(smsContactServiceMock, times(1)).backfillMissingContacts();
  }
}
