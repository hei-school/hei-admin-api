package school.hei.haapi.endpoint.event;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.internet.AddressException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.model.CheckParticipantMissedEventTriggered;
import school.hei.haapi.endpoint.event.model.MissedEventEmail;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.FakeDataProvider;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.dao.EventDao;
import school.hei.haapi.service.EventParticipantService;
import school.hei.haapi.service.EventService;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.event.CheckParticipantMissedEventService;
import school.hei.haapi.service.event.MissedEventEmailService;

class CheckParticipantMissingEventTest extends FacadeITMockedThirdParties {
  @MockBean private EventDao eventDao;
  @MockBean private EventParticipantService eventParticipantService;
  @MockBean private EventProducer<MissedEventEmail> eventProducer;
  @MockBean private UserService userService;
  @MockBean private EventService eventService;
  @MockBean private Mailer mailer;
  @Autowired private FakeDataProvider fakeDataProvider;
  private MissedEventEmailService missedEventEmailService;
  private CheckParticipantMissedEventService subject;

  @BeforeEach
  void setup() {
    subject =
        new CheckParticipantMissedEventService(eventDao, eventParticipantService, eventProducer);
    missedEventEmailService = new MissedEventEmailService(userService, eventService, mailer);
  }

  @Test
  void participant_missing_event_received_email() {
    when(eventDao.findByCriteria(
            eq(null),
            any(Instant.class),
            any(Instant.class),
            eq(null),
            eq(null),
            eq(null),
            eq(null)))
        .thenReturn(List.of(fakeDataProvider.someEvent(), fakeDataProvider.someEvent()));

    subject.accept(new CheckParticipantMissedEventTriggered());

    verify(eventParticipantService, times(2)).findByEventId(anyString(), eq(null));
    verify(eventProducer, times(1)).accept(any());
  }

  @Test
  void send_mail_for_missed_event() {
    when(userService.getById(anyString())).thenReturn(new User());
    when(eventService.findEventById(anyString())).thenReturn(fakeDataProvider.someEvent());

    assertThrows(
        AddressException.class,
        () -> missedEventEmailService.accept(new MissedEventEmail("", "", "bad email")));
    missedEventEmailService.accept(new MissedEventEmail("", "", "email@gmail.com"));

    verify(mailer, times(1)).accept(any());
  }
}
