package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.endpoint.rest.model.EventType.COURSE;
import static school.hei.haapi.endpoint.rest.model.EventType.INTEGRATION;
import static school.hei.haapi.endpoint.rest.model.FrequencyScopeDay.MONDAY;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.StudentIT.student2;
import static school.hei.haapi.integration.StudentIT.student3;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCreatableEventByManager1;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.EVENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EVENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.createEventCourse1;
import static school.hei.haapi.integration.conf.TestUtils.createIntegrationEvent;
import static school.hei.haapi.integration.conf.TestUtils.event1;
import static school.hei.haapi.integration.conf.TestUtils.event2;
import static school.hei.haapi.integration.conf.TestUtils.event3;
import static school.hei.haapi.integration.conf.TestUtils.group1;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.conf.TestUtils.student1AttendEvent2;
import static school.hei.haapi.integration.conf.TestUtils.student1MissEvent1;
import static school.hei.haapi.integration.conf.TestUtils.student2AttendEvent2;
import static school.hei.haapi.integration.conf.TestUtils.student3AttendEvent1;
import static school.hei.haapi.integration.conf.TestUtils.student3MissEvent2;

import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.EventsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateEvent;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.model.EventAttendance;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.endpoint.rest.model.EventParticipantStats;
import school.hei.haapi.endpoint.rest.model.UpdateEventParticipant;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

@Slf4j
@Testcontainers
@AutoConfigureMockMvc
public class EventIT extends FacadeITMockedThirdParties {

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void attempt_to_create_a_frequency_with_missing_data_ko() {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Frequency cannot be created without number"
            + " of\"}",
        () ->
            api.crupdateEvents(
                List.of(createEventCourse1(), createIntegrationEvent()),
                MONDAY,
                null,
                "09:00",
                "12:00"));
  }

  @Test
  void attempt_to_create_a_frequency_with_invalid_hour_ko() {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Hour must be of format HH:MM\"}",
        () ->
            api.crupdateEvents(
                List.of(createEventCourse1(), createIntegrationEvent()),
                MONDAY,
                2,
                "9:00",
                "12:00"));
  }

  @Test
  void manager_read_event_ok() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    var actual = api.getEvents(1, 500, null, null, null, null, null, null);

    System.out.println(actual);
    assertTrue(actual.containsAll(List.of(event1(), event2(), event3())));

    var eventsBeginAfterAnInstant =
        api.getEvents(
            1, 15, Instant.parse("2022-12-15T10:00:00.00Z"), null, null, null, null, null);

    assertTrue(eventsBeginAfterAnInstant.contains(event1()));
    assertFalse(eventsBeginAfterAnInstant.contains(event2()));

    List<Event> eventsBeginBetweenTwoInstant =
        api.getEvents(
            1,
            15,
            Instant.parse("2022-12-07T08:00:00.00Z"),
            Instant.parse("2022-12-10T08:00:00.00Z"),
            null,
            null,
            null,
            null);

    assertTrue(eventsBeginBetweenTwoInstant.containsAll(List.of(event2(), event3())));
    assertFalse(eventsBeginBetweenTwoInstant.contains(event1()));

    var eventsBeginBeforeAnInstant =
        api.getEvents(
            1, 15, null, Instant.parse("2022-12-08T08:00:00.00Z"), null, null, null, null);

    assertTrue(eventsBeginBeforeAnInstant.contains(event2()));
    assertFalse(eventsBeginBeforeAnInstant.containsAll(List.of(event1(), event3())));

    var eventsFilterByType = api.getEvents(1, 15, null, null, COURSE, null, null, null);
    assertTrue(eventsFilterByType.contains(event1()));
    assertFalse(eventsFilterByType.contains(event3()));
    assertFalse(eventsFilterByType.contains(event2()));

    var eventsFilterByTitle = api.getEvents(1, 15, null, null, null, "PROG1", null, null);
    assertTrue(eventsFilterByTitle.contains(event1()));
    assertFalse(eventsFilterByTitle.contains(event3()));
    assertFalse(eventsFilterByTitle.contains(event2()));
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

    List<EventParticipant> actual =
        api.getEventParticipants(EVENT1_ID, 1, 15, null, null, null, null);

    assertTrue(actual.contains(student1MissEvent1()));
    assertTrue(actual.contains(student3AttendEvent1()));
    assertFalse(actual.contains(student1AttendEvent2()));

    List<EventParticipant> participantsFilteredByGroupRef =
        api.getEventParticipants(EVENT2_ID, 1, 15, "G2", null, null, null);

    // Notice :
    // Student 1 and Student 3 are in GROUP 1
    // Student 2 is in GROUP 2

    assertTrue(participantsFilteredByGroupRef.contains(student2AttendEvent2()));
    assertFalse(participantsFilteredByGroupRef.contains(student1AttendEvent2()));
    assertFalse(participantsFilteredByGroupRef.contains(student3MissEvent2()));
  }

  @Test
  void manager_read_event_participant_with_criteria_ok() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    // Notice :
    // Student 1 and Student 3 are in GROUP 1
    // Student 2 is in GROUP 2

    // Test the ref filter

    List<EventParticipant> participantsFilteredByRef =
        api.getEventParticipants(EVENT2_ID, 1, 15, null, student2().getRef(), null, null);

    assertEquals(participantsFilteredByRef.getFirst(), student2AttendEvent2());

    // Test the name filter

    List<EventParticipant> participantsFilteredByName =
        api.getEventParticipants(EVENT2_ID, 1, 15, null, null, student2().getLastName(), null);

    assertTrue(participantsFilteredByName.contains(student2AttendEvent2()));

    // Test the status filter

    List<EventParticipant> participantsFilteredByStatus =
        api.getEventParticipants(EVENT2_ID, 1, 15, null, null, null, MISSING);

    assertEquals(student3MissEvent2().getId(), participantsFilteredByStatus.getFirst().getId());
  }

  @Test
  void student_create_or_update_event_or_event_participant_ko() {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    assertThrowsForbiddenException(
        () -> api.crupdateEvents(List.of(createEventCourse1()), null, null, null, null));
    assertThrowsForbiddenException(
        () -> api.updateEventParticipantsStatus(EVENT1_ID, List.of(new UpdateEventParticipant())));
  }

  @Test
  void student_delete_event_ko() {
    EventsApi api = new EventsApi(anApiClient(STUDENT1_TOKEN));
    assertThrowsForbiddenException(() -> api.deleteEventById(EVENT1_ID));
  }

  @Test
  void manager_delete_event_ok() throws ApiException {
    EventsApi api = new EventsApi(anApiClient(MANAGER1_TOKEN));
    List<Event> events =
        api.crupdateEvents(
            List.of(someCreatableEventByManager1(INTEGRATION)), null, null, null, null);
    Event deletedEvent = api.deleteEventById(events.getFirst().getId());
    assertEquals(events.getFirst().getId(), deletedEvent.getId());
  }

  @Test
  void student_get_event_stats_ko() {
    EventsApi studentApi = new EventsApi(anApiClient(STUDENT1_TOKEN));
    assertThrowsForbiddenException(() -> studentApi.getEventStats(null, null, null));
  }

  @Test
  void manager_get_overall_stats_ok() {
    EventsApi managerApi = new EventsApi(anApiClient(MANAGER1_TOKEN));
    assertDoesNotThrow(() -> managerApi.getEventStats(null, null, null));
  }

  @Test
  void admin_get_overall_stats_ok() {
    EventsApi managerApi = new EventsApi(anApiClient(ADMIN1_TOKEN));
    assertDoesNotThrow(() -> managerApi.getEventStats(null, null, null));
  }

  @Test
  void student_get_stats_ko() {
    EventsApi studentApi = new EventsApi(anApiClient(STUDENT1_TOKEN));

    assertThrowsForbiddenException(
        () -> studentApi.getEventParticipantStats(STUDENT1_ID, null, null));
  }

  @Test
  void get_stats_ok() throws ApiException {
    EventsApi managerApi = new EventsApi(anApiClient(MANAGER1_TOKEN));
    // TODO: create dynamically some events during test and apply filters to get stats for these
    // events
    EventParticipantStats eventParticipantStats =
        managerApi.getEventParticipantStats(STUDENT1_ID, null, null);
    assertEquals(2, eventParticipantStats.getTotalEvents());
  }

  @Test
  void event_as_public_link() throws ApiException {
    EventsApi api = new EventsApi(anApiClient(null));
    var actual = api.getEvents(1, 15, null, null, null, null, null, null);
    assertEquals(event1(), actual.getFirst());
    assertEquals(event3(), actual.get(1));
    assertEquals(event2(), actual.get(2));
  }

  @Test
  void get_event_attendance() throws ApiException {
    EventsApi api = new EventsApi(anApiClient(MANAGER1_TOKEN));

    List<EventAttendance> eventParticipants =
        api.getAllEventParticipants(null, 1, 10, null, null, null, null, null, null);
    List<EventAttendance> eventParticipantsWithAllFilter =
        api.getAllEventParticipants(
            null,
            null,
            null,
            Instant.parse("2022-12-08T07:59:59.00Z"),
            Instant.parse("2022-12-08T08:00:01.00Z"),
            PRESENT,
            student1().getGroups().getFirst().getRef(),
            student1().getRef(),
            student1().getFirstName());
    List<EventAttendance> eventParticipantsInEventDateRange =
        api.getAllEventParticipants(
            null,
            null,
            null,
            Instant.parse("2022-12-08T07:59:59.00Z"),
            Instant.parse("2022-12-08T08:00:01.00Z"),
            null,
            null,
            null,
            null);
    List<EventAttendance> statusFilteredEventParticipants =
        api.getAllEventParticipants(null, null, null, null, null, MISSING, null, null, null);
    List<EventAttendance> groupFilteredEventParticipants =
        api.getAllEventParticipants(
            null, null, null, null, null, null, group1().getRef(), null, null);
    List<EventAttendance> studentRefFilteredEventParticipants =
        api.getAllEventParticipants(
            null, null, null, null, null, null, null, student3().getRef(), null);
    List<EventAttendance> studentNameFilteredEventParticipants =
        api.getAllEventParticipants(
            null, null, null, null, null, null, null, null, student3().getFirstName());

    assertTrue(
        eventParticipants.contains(
            new EventAttendance().event(event2()).eventParticipant(student3MissEvent2())));
    assertEquals(
        student1AttendEvent2(), eventParticipantsWithAllFilter.getFirst().getEventParticipant());
    assertEquals(
        student1AttendEvent2(), eventParticipantsInEventDateRange.getFirst().getEventParticipant());
    assertEquals(
        student1MissEvent1(), statusFilteredEventParticipants.getFirst().getEventParticipant());
    assertEquals(
        student1MissEvent1(), groupFilteredEventParticipants.getFirst().getEventParticipant());
    assertEquals(
        student3AttendEvent1(),
        studentRefFilteredEventParticipants.getFirst().getEventParticipant());
    assertEquals(
        student3AttendEvent1(),
        studentNameFilteredEventParticipants.getFirst().getEventParticipant());
  }

  @Test
  void manager_create_event_with_is_online_ok() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    CreateEvent onlineEvent = someCreatableEventByManager1(INTEGRATION).isOnline(true);
    List<Event> createdOnlineEvents =
        api.crupdateEvents(List.of(onlineEvent), null, null, null, null);
    Event createdOnline = createdOnlineEvents.getFirst();

    CreateEvent offlineEvent = someCreatableEventByManager1(INTEGRATION).isOnline(false);
    List<Event> createdOfflineEvents =
        api.crupdateEvents(List.of(offlineEvent), null, null, null, null);
    Event createdOffline = createdOfflineEvents.getFirst();

    Event actualOnline = api.getEventById(createdOnline.getId());
    assertEquals(Boolean.TRUE, actualOnline.getIsOnline(), "Event should be marked as online");
    assertEquals(createdOnline.getId(), actualOnline.getId());

    Event actualOffline = api.getEventById(createdOffline.getId());
    assertNotEquals(Boolean.TRUE, actualOffline.getIsOnline(), "Event should be marked as offline");
    assertEquals(createdOffline.getId(), actualOffline.getId());
  }

  @Test
  void manager_create_event_with_is_online_null_defaults_to_false() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(apiClient);

    CreateEvent eventWithNullIsOnline = someCreatableEventByManager1(INTEGRATION);

    List<Event> createdEvents =
        api.crupdateEvents(List.of(eventWithNullIsOnline), null, null, null, null);
    Event created = createdEvents.getFirst();

    Event actual = api.getEventById(created.getId());

    assertNotEquals(
        Boolean.TRUE,
        actual.getIsOnline(),
        "Event should default to offline when isOnline is null");
    assertEquals(created.getId(), actual.getId());
  }
}
