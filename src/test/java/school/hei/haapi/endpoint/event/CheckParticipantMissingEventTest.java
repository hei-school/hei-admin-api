package school.hei.haapi.endpoint.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;
import static school.hei.haapi.endpoint.rest.model.EventType.SEMINAR;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.model.CheckParticipantMissedEventTriggered;
import school.hei.haapi.endpoint.event.model.MissedEventEmail;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.dao.EventDao;
import school.hei.haapi.service.EventParticipantService;
import school.hei.haapi.service.event.CheckParticipantMissedEventService;

class CheckParticipantMissingEventTest extends FacadeITMockedThirdParties {
  @MockBean private EventDao eventDao;
  @MockBean private EventParticipantService eventParticipantService;
  @MockBean private EventProducer<MissedEventEmail> eventProducer;
  private CheckParticipantMissedEventService subject;

  @BeforeEach
  void setup() {
    subject =
        new CheckParticipantMissedEventService(eventDao, eventParticipantService, eventProducer);
  }

  @Test
  void participant_missing_event_received_email() {
    Event event1 = new Event("", SEMINAR, null, null, null, false, null, null, null, null, null);
    when(eventDao.findByCriteria(
            eq(null), any(Instant.class), any(Instant.class), eq(null), eq(null), eq(null)))
        .thenReturn(List.of(event1));
    when(eventParticipantService.findByEventId(anyString(), eq(null)))
        .thenReturn(
            List.of(
                new EventParticipant("participant1", event1, new User(), MISSING, null),
                new EventParticipant("participant2", event1, new User(), MISSING, null)));

    // This function modify nothing, just test if not throw
    assertDoesNotThrow(() -> subject.accept(new CheckParticipantMissedEventTriggered()));
  }
}
