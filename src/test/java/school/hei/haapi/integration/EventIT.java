package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.EventsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdateEventParticipant;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.endpoint.rest.model.EventType.COURSE;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.EVENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EVENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.GROUP2_REF;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.createEventCourse1;
import static school.hei.haapi.integration.conf.TestUtils.event1;
import static school.hei.haapi.integration.conf.TestUtils.event2;
import static school.hei.haapi.integration.conf.TestUtils.event3;
import static school.hei.haapi.integration.conf.TestUtils.expectedCourseEventCreated;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.conf.TestUtils.student1AttendEvent2;
import static school.hei.haapi.integration.conf.TestUtils.student1MissEvent1;
import static school.hei.haapi.integration.conf.TestUtils.student2AttendEvent2;
import static school.hei.haapi.integration.conf.TestUtils.student3AttendEvent1;
import static school.hei.haapi.integration.conf.TestUtils.student3MissEvent2;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = EventIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class EventIT extends MockedThirdParties {

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, EventIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void manager_create_event_ok() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    List<Event> actual = api.crupdateEvents(List.of(createEventCourse1()));

    Event event = actual.get(0);

    assertEquals(expectedCourseEventCreated().getType(), event.getType());
    assertEquals(expectedCourseEventCreated().getEnd(), event.getEnd());
    assertEquals(expectedCourseEventCreated().getBegin(), event.getBegin());
    assertEquals(expectedCourseEventCreated().getDescription(), event.getDescription());

    List<EventParticipant> actualEventParticipantCreated =
        api.getEventParticipants(event.getId(), 1, 15, null);
  }

  @Test
  void manager_read_event_ok() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    List<Event> actual = api.getEvents(1, 15, null, null, null);

    assertTrue(actual.containsAll(List.of(event1(), event2(), event3())));

    List<Event> eventsBeginAfterAnInstant =
        api.getEvents(1, 15, Instant.parse("2022-12-15T10:00:00.00Z"), null, null);

    assertTrue(eventsBeginAfterAnInstant.contains(event1()));
    assertFalse(eventsBeginAfterAnInstant.contains(event2()));

    List<Event> eventsBeginBetweenTwoInstant =
        api.getEvents(
            1,
            15,
            Instant.parse("2022-12-07T08:00:00.00Z"),
            Instant.parse("2022-12-10T08:00:00.00Z"),
            null);

    assertTrue(eventsBeginBetweenTwoInstant.containsAll(List.of(event2(), event3())));
    assertFalse(eventsBeginBetweenTwoInstant.contains(event1()));

    List<Event> eventsBeginBeforeAnInstant =
        api.getEvents(1, 15, null, Instant.parse("2022-12-08T08:00:00.00Z"), null);

    assertTrue(eventsBeginBeforeAnInstant.contains(event2()));
    assertFalse(eventsBeginBeforeAnInstant.containsAll(List.of(event1(), event3())));

    List<Event> eventsFilterByType = api.getEvents(1, 15, null, null, COURSE);
    assertTrue(eventsFilterByType.contains(event1()));
    assertFalse(eventsFilterByType.contains(event3()));
    assertFalse(eventsFilterByType.contains(event2()));


  }

  @Test
  void manager_read_event_by_id_ok() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    Event actual = api.getEventById(EVENT1_ID);

    assertEquals(event1(), actual);
  }

  @Test
  void manager_read_event_participant_ok() throws ApiException {

    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    List<EventParticipant> actual = api.getEventParticipants(EVENT1_ID, 1, 15, null);

    assertTrue(actual.contains(student1MissEvent1()));
    assertTrue(actual.contains(student3AttendEvent1()));
    assertFalse(actual.contains(student1AttendEvent2()));

    List<EventParticipant> participantsFilteredByGroupRef =
        api.getEventParticipants(EVENT2_ID, 1, 15, GROUP2_REF);

    // Notice :
    // Student 1 and Student 3 are in GROUP 1
    // Student 2 is in GROUP 2

    assertTrue(participantsFilteredByGroupRef.contains(student2AttendEvent2()));
    assertFalse(participantsFilteredByGroupRef.contains(student1AttendEvent2()));
    assertFalse(participantsFilteredByGroupRef.contains(student3MissEvent2()));
  }

  @Test
  void manager_create_or_update_event_participant_ok() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    CrupdateEventParticipant createEventParticipant =
        new CrupdateEventParticipant()
            .eventStatus(MISSING)
            .email(student1().getEmail())
            .ref(student1().getRef())
            .nic(student1().getNic())
            .firstName(student1().getFirstName())
            .lastName(student1().getLastName())
            .groupId(GROUP1_ID);

    List<EventParticipant> actualCreated =
        api.crupdateEventParticipants(EVENT1_ID, List.of(createEventParticipant));

    EventParticipant actualEventParticipant = actualCreated.get(0);

    assertEquals(MISSING, actualEventParticipant.getEventStatus());
    assertEquals(student1MissEvent1().getEmail(), actualEventParticipant.getEmail());
    assertEquals(student1().getNic(), actualEventParticipant.getNic());

    List<EventParticipant> updateEventParticipant =
        api.crupdateEventParticipants(
            EVENT1_ID,
            List.of(
                createEventParticipant.id(actualEventParticipant.getId()).eventStatus(PRESENT)));

    assertEquals(PRESENT, updateEventParticipant.get(0).getEventStatus());
  }

  @Test
  void student_create_or_update_event_or_event_participant_ko() throws ApiException {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    assertThrowsForbiddenException(() -> api.crupdateEvents(List.of(createEventCourse1())));
    assertThrowsForbiddenException(
        () -> api.crupdateEventParticipants(EVENT1_ID, List.of(new CrupdateEventParticipant())));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
