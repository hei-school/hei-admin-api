package school.hei.haapi.unit.sms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.SmsContactBackfillTriggered;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.User;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.event.SmsContactBackfillTriggeredService;
import school.hei.haapi.service.sms.SmsContactService;

class SmsContactBackfillTriggeredServiceTest {
  private final UserService userServiceMock = mock();
  private final SmsContactService smsContactServiceMock = mock();

  private final SmsContactBackfillTriggeredService subject =
      new SmsContactBackfillTriggeredService(userServiceMock, smsContactServiceMock);

  @Test
  void no_enabled_user_creates_nothing() {
    when(userServiceMock.getAllEnabledUsers()).thenReturn(List.of());

    subject.accept(new SmsContactBackfillTriggered());

    verify(smsContactServiceMock, never()).createContactIfMissing(any());
  }

  @Test
  void attempts_a_contact_for_every_enabled_user_regardless_of_whether_one_already_exists() {
    var withContact = User.builder().id("u1").build();
    var withoutContact = User.builder().id("u2").build();
    when(userServiceMock.getAllEnabledUsers()).thenReturn(List.of(withContact, withoutContact));
    when(smsContactServiceMock.createContactIfMissing(withContact)).thenReturn(Optional.empty());
    when(smsContactServiceMock.createContactIfMissing(withoutContact))
        .thenReturn(Optional.of(SmsContact.builder().id("c2").build()));

    subject.accept(new SmsContactBackfillTriggered());

    verify(smsContactServiceMock, times(1)).createContactIfMissing(withContact);
    verify(smsContactServiceMock, times(1)).createContactIfMissing(withoutContact);
  }
}
