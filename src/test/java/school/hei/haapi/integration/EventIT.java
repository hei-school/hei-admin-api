package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.endpoint.rest.model.EventType.COURSE;
import static school.hei.haapi.endpoint.rest.model.EventType.INTEGRATION;
import static school.hei.haapi.endpoint.rest.model.EventType.SEMINAR;
import static school.hei.haapi.endpoint.rest.model.FrequencyScopeDay.MONDAY;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.testData.CourseTestData.prog1;
import static school.hei.haapi.integration.testData.EventTestData.aParticipant;
import static school.hei.haapi.integration.testData.EventTestData.anEvent;
import static school.hei.haapi.integration.testData.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.GroupTestData.g2;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.integration.testData.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.api.EventsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateEvent;
import school.hei.haapi.endpoint.rest.model.EventLocation;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.endpoint.rest.model.GroupIdentifier;
import school.hei.haapi.endpoint.rest.model.PlaceEnum;
import school.hei.haapi.endpoint.rest.model.RoomEnum;
import school.hei.haapi.endpoint.rest.model.UpdateEventParticipant;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;

public class EventIT extends FacadeITMockedThirdParties {
  /** Every event of this test begins inside this window, so date filters can isolate them. */
  private static final Instant COURSE_EVENT_BEGIN = Instant.parse("2026-06-20T08:00:00.00Z");

  private static final Instant INTEGRATION_EVENT_BEGIN = Instant.parse("2026-06-08T08:00:00.00Z");
  private static final Instant SEMINAR_EVENT_BEGIN = Instant.parse("2026-06-09T08:00:00.00Z");

  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private EventRepository eventRepository;
  @Autowired private EventParticipantRepository eventParticipantRepository;

  private User studentAxel;
  private User studentFreddy;
  private User studentTolojanahary;
  private User teacherToky;
  private User managerHasina;
  private User adminUser;
  private Group groupOne;
  private Group groupTwo;
  private Course courseProg1;
  private CourseAssignment assignment;
  private GroupFlow axelJoinsOne;
  private GroupFlow tolojanaharyJoinsOne;
  private GroupFlow freddyJoinsTwo;

  private Event courseEvent;
  private Event integrationEvent;
  private Event seminarEvent;

  private EventParticipant axelMissesCourseEvent;
  private EventParticipant tolojanaharyAttendsCourseEvent;
  private EventParticipant axelAttendsIntegration;
  private EventParticipant freddyAttendsIntegration;
  private EventParticipant tolojanaharyMissesIntegration;

  /** Events the tests create through the API, swept in tearDown. */
  private final List<String> createdEventIds = new ArrayList<>();

  private String axelToken;
  private String managerToken;
  private String adminToken;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    studentFreddy = userRepository.save(freddy());
    studentTolojanahary = userRepository.save(tolojanahary());
    teacherToky = userRepository.save(toky());
    managerHasina = userRepository.save(hasina());
    adminUser = userRepository.save(adminMialy());

    groupOne = groupRepository.save(g1());
    groupTwo = groupRepository.save(g2());
    axelJoinsOne = groupFlowRepository.save(createGroupFlow(studentAxel, groupOne));
    tolojanaharyJoinsOne = groupFlowRepository.save(createGroupFlow(studentTolojanahary, groupOne));
    freddyJoinsTwo = groupFlowRepository.save(createGroupFlow(studentFreddy, groupTwo));

    courseProg1 = courseRepository.save(prog1());
    assignment =
        courseAssignmentRepository.save(
            createCourseAssignment(courseProg1, teacherToky, List.of(groupOne, groupTwo)));

    var course =
        anEvent(
            managerHasina,
            COURSE,
            "PROG1",
            COURSE_EVENT_BEGIN,
            COURSE_EVENT_BEGIN.plusSeconds(7200));
    course.setCourse(courseProg1);
    course.setGroups(new ArrayList<>(List.of(groupOne)));
    courseEvent = eventRepository.save(course);

    var integration =
        anEvent(
            managerHasina,
            INTEGRATION,
            "Integration Day",
            INTEGRATION_EVENT_BEGIN,
            INTEGRATION_EVENT_BEGIN.plusSeconds(14400));
    integration.setGroups(new ArrayList<>(List.of(groupOne, groupTwo)));
    integrationEvent = eventRepository.save(integration);

    var seminar =
        anEvent(
            teacherToky,
            SEMINAR,
            "December Seminar",
            SEMINAR_EVENT_BEGIN,
            SEMINAR_EVENT_BEGIN.plusSeconds(14400));
    seminar.setGroups(new ArrayList<>(List.of(groupOne)));
    seminarEvent = eventRepository.save(seminar);

    axelMissesCourseEvent =
        eventParticipantRepository.save(aParticipant(courseEvent, studentAxel, groupOne, MISSING));
    tolojanaharyAttendsCourseEvent =
        eventParticipantRepository.save(
            aParticipant(courseEvent, studentTolojanahary, groupOne, PRESENT));
    axelAttendsIntegration =
        eventParticipantRepository.save(
            aParticipant(integrationEvent, studentAxel, groupOne, PRESENT));
    freddyAttendsIntegration =
        eventParticipantRepository.save(
            aParticipant(integrationEvent, studentFreddy, groupTwo, PRESENT));
    tolojanaharyMissesIntegration =
        eventParticipantRepository.save(
            aParticipant(integrationEvent, studentTolojanahary, groupOne, MISSING));
  }

  @BeforeEach
  void setUp() {
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    adminToken = tokenFor(casdoorAuthServiceMock, adminUser);
  }

  @AfterEach
  void tearDown() {
    eventParticipantRepository.deleteAll(
        List.of(
            axelMissesCourseEvent,
            tolojanaharyAttendsCourseEvent,
            axelAttendsIntegration,
            freddyAttendsIntegration,
            tolojanaharyMissesIntegration));
    eventRepository.deleteAllById(createdEventIds);
    createdEventIds.clear();
    eventRepository.deleteAll(List.of(courseEvent, integrationEvent, seminarEvent));
    courseAssignmentRepository.deleteById(assignment.getId());
    courseRepository.deleteById(courseProg1.getId());
    groupFlowRepository.deleteAll(List.of(axelJoinsOne, tolojanaharyJoinsOne, freddyJoinsTwo));
    groupRepository.deleteAll(List.of(groupOne, groupTwo));
    userRepository.deleteAll(
        List.of(
            studentAxel,
            studentFreddy,
            studentTolojanahary,
            teacherToky,
            managerHasina,
            adminUser));
  }

  private EventsApi apiAs(String token) {
    return new EventsApi(anApiClient(token));
  }

  private CreateEvent aCreatableEvent(EventType type) {
    return new CreateEvent()
        .id("event" + randomUUID() + "_id")
        .courseId(courseProg1.getId())
        .beginDatetime(Instant.parse("2026-07-08T08:00:00.00Z"))
        .endDatetime(Instant.parse("2026-07-08T10:00:00.00Z"))
        .description("Another event")
        .eventType(type)
        .plannerId(managerHasina.getId())
        .location(new EventLocation().place(PlaceEnum.IVANDRY).room(RoomEnum.UNKNOWN))
        .groups(List.of(new GroupIdentifier().id(groupOne.getId()).ref(groupOne.getRef())));
  }

  private static List<String> eventIdsOf(List<school.hei.haapi.endpoint.rest.model.Event> events) {
    return events.stream().map(event -> event.getId()).toList();
  }

  private static List<String> participantIdsOf(
      List<school.hei.haapi.endpoint.rest.model.EventParticipant> participants) {
    return participants.stream().map(participant -> participant.getId()).toList();
  }

  @Test
  void attempt_to_create_a_frequency_with_missing_data_ko() {
    var api = apiAs(managerToken);
    var events = List.of(aCreatableEvent(COURSE), aCreatableEvent(INTEGRATION));

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Frequency cannot be created without number"
            + " of\"}",
        () -> api.crupdateEvents(events, MONDAY, null, "09:00", "12:00"));
  }

  @Test
  void attempt_to_create_a_frequency_with_invalid_hour_ko() {
    var api = apiAs(managerToken);
    var events = List.of(aCreatableEvent(COURSE), aCreatableEvent(INTEGRATION));

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Hour must be of format HH:MM\"}",
        () -> api.crupdateEvents(events, MONDAY, 2, "9:00", "12:00"));
  }

  @Test
  void manager_read_event_ok() throws ApiException {
    var api = apiAs(managerToken);

    var all = api.getEvents(1, 500, null, null, null, null, null, null, null);
    assertTrue(
        eventIdsOf(all)
            .containsAll(
                List.of(courseEvent.getId(), integrationEvent.getId(), seminarEvent.getId())));

    var beginningAfter =
        api.getEvents(
            1, 500, COURSE_EVENT_BEGIN.minusSeconds(1), null, null, null, null, null, null);
    assertTrue(eventIdsOf(beginningAfter).contains(courseEvent.getId()));
    assertFalse(eventIdsOf(beginningAfter).contains(integrationEvent.getId()));

    var beginningBetween =
        api.getEvents(
            1,
            500,
            INTEGRATION_EVENT_BEGIN.minusSeconds(1),
            SEMINAR_EVENT_BEGIN.plusSeconds(1),
            null,
            null,
            null,
            null,
            null);
    assertTrue(
        eventIdsOf(beginningBetween)
            .containsAll(List.of(integrationEvent.getId(), seminarEvent.getId())));
    assertFalse(eventIdsOf(beginningBetween).contains(courseEvent.getId()));

    var byType = api.getEvents(1, 500, null, null, COURSE, null, null, null, null);
    assertTrue(eventIdsOf(byType).contains(courseEvent.getId()));
    assertFalse(eventIdsOf(byType).contains(integrationEvent.getId()));

    var byTitle = api.getEvents(1, 500, null, null, null, courseEvent.getTitle(), null, null, null);
    assertTrue(eventIdsOf(byTitle).contains(courseEvent.getId()));
    assertFalse(eventIdsOf(byTitle).contains(integrationEvent.getId()));
  }

  @Test
  void manager_read_event_by_id_ok() throws ApiException {
    var actual = apiAs(managerToken).getEventById(courseEvent.getId());

    assertEquals(courseEvent.getId(), actual.getId());
    assertEquals(courseEvent.getTitle(), actual.getTitle());
  }

  @Test
  void manager_read_event_participant_ok() throws ApiException {
    var api = apiAs(managerToken);

    var courseEventParticipants =
        api.getEventParticipants(courseEvent.getId(), 1, 50, null, null, null, null);
    assertTrue(participantIdsOf(courseEventParticipants).contains(axelMissesCourseEvent.getId()));
    assertTrue(
        participantIdsOf(courseEventParticipants).contains(tolojanaharyAttendsCourseEvent.getId()));
    assertFalse(participantIdsOf(courseEventParticipants).contains(axelAttendsIntegration.getId()));

    var byGroupRef =
        api.getEventParticipants(
            integrationEvent.getId(), 1, 50, groupTwo.getRef(), null, null, null);
    assertTrue(participantIdsOf(byGroupRef).contains(freddyAttendsIntegration.getId()));
    assertFalse(participantIdsOf(byGroupRef).contains(axelAttendsIntegration.getId()));
  }

  @Test
  void manager_read_event_participant_with_criteria_ok() throws ApiException {
    var api = apiAs(managerToken);

    var byRef =
        api.getEventParticipants(
            integrationEvent.getId(), 1, 50, null, studentFreddy.getRef(), null, null);
    assertEquals(List.of(freddyAttendsIntegration.getId()), participantIdsOf(byRef));

    var byName =
        api.getEventParticipants(
            integrationEvent.getId(), 1, 50, null, null, studentFreddy.getLastName(), null);
    assertTrue(participantIdsOf(byName).contains(freddyAttendsIntegration.getId()));

    var byStatus =
        api.getEventParticipants(integrationEvent.getId(), 1, 50, null, null, null, MISSING);
    assertEquals(List.of(tolojanaharyMissesIntegration.getId()), participantIdsOf(byStatus));
  }

  @Test
  void student_create_or_update_event_or_event_participant_ko() {
    var api = apiAs(axelToken);
    var event = aCreatableEvent(COURSE);

    assertThrowsForbiddenException(
        () -> api.crupdateEvents(List.of(event), null, null, null, null));
    assertThrowsForbiddenException(
        () ->
            api.updateEventParticipantsStatus(
                courseEvent.getId(), List.of(new UpdateEventParticipant())));
  }

  @Test
  void student_delete_event_ko() {
    var api = apiAs(axelToken);

    assertThrowsForbiddenException(() -> api.deleteEventById(courseEvent.getId()));
  }

  @Test
  void manager_delete_event_ok() throws ApiException {
    var api = apiAs(managerToken);

    var events = api.crupdateEvents(List.of(aCreatableEvent(INTEGRATION)), null, null, null, null);
    var deletedEvent = api.deleteEventById(events.getFirst().getId());

    assertEquals(events.getFirst().getId(), deletedEvent.getId());
  }

  @Test
  void student_get_event_stats_ko() {
    var api = apiAs(axelToken);

    assertThrowsForbiddenException(() -> api.getEventStats(null, null, null));
  }

  @Test
  void manager_get_overall_stats_ok() {
    var api = apiAs(managerToken);

    assertDoesNotThrow(() -> api.getEventStats(null, null, null));
  }

  @Test
  void admin_get_overall_stats_ok() {
    var api = apiAs(adminToken);

    assertDoesNotThrow(() -> api.getEventStats(null, null, null));
  }

  @Test
  void student_get_stats_ko() {
    var api = apiAs(axelToken);

    assertThrowsForbiddenException(
        () -> api.getEventParticipantStats(studentAxel.getId(), null, null));
  }

  @Test
  void get_stats_ok() throws ApiException {
    var stats = apiAs(managerToken).getEventParticipantStats(studentAxel.getId(), null, null);
    assertEquals(2, stats.getTotalEvents());
  }

  @Test
  void event_as_public_link() throws ApiException {
    var api = new EventsApi(anApiClient(null));

    var actual = api.getEvents(1, 500, null, null, null, null, null, null, null);
    var ownEvents =
        actual.stream()
            .filter(
                e ->
                    List.of(courseEvent.getId(), integrationEvent.getId(), seminarEvent.getId())
                        .contains(e.getId()))
            .sorted(
                Comparator.comparing(school.hei.haapi.endpoint.rest.model.Event::getBeginDatetime)
                    .reversed())
            .toList();

    assertEquals(
        List.of(courseEvent.getId(), seminarEvent.getId(), integrationEvent.getId()),
        eventIdsOf(ownEvents));
  }

  @Test
  void event_as_public_link_filter_by_groupRef() throws ApiException {
    var api = new EventsApi(anApiClient(null));

    var actual =
        api.getEvents(1, 500, null, null, null, null, null, null, List.of(groupTwo.getRef()));

    assertTrue(eventIdsOf(actual).contains(integrationEvent.getId()));
    assertFalse(eventIdsOf(actual).contains(seminarEvent.getId()));
  }

  @Test
  void event_as_private_link_filter_by_groupRef() throws ApiException {
    var api = apiAs(managerToken);

    var actual =
        api.getEvents(1, 500, null, null, null, null, null, null, List.of(groupTwo.getRef()));

    assertTrue(eventIdsOf(actual).contains(integrationEvent.getId()));
    assertFalse(eventIdsOf(actual).contains(seminarEvent.getId()));
  }

  @Test
  void filter_events_by_teacher_id_OK() throws ApiException {
    var api = apiAs(managerToken);

    var actual =
        api.getEvents(
            1, 500, null, null, null, null, null, teacherToky.getId(), List.of(groupOne.getRef()));

    // the filter matches the teacher assigned to the event's course, not the event planner: the
    // seminar is planned by toky but carries no course, so it is excluded
    assertEquals(List.of(courseEvent.getId()), eventIdsOf(actual));
  }

  @Test
  void get_event_attendance() throws ApiException {
    var api = apiAs(managerToken);

    var inEventDateRange =
        api.getAllEventParticipants(
            null,
            null,
            null,
            INTEGRATION_EVENT_BEGIN.minusSeconds(1),
            INTEGRATION_EVENT_BEGIN.plusSeconds(1),
            null,
            null,
            null,
            null);
    assertTrue(
        inEventDateRange.stream()
            .anyMatch(a -> axelAttendsIntegration.getId().equals(a.getEventParticipant().getId())));

    var withAllFilters =
        api.getAllEventParticipants(
            null,
            null,
            null,
            INTEGRATION_EVENT_BEGIN.minusSeconds(1),
            INTEGRATION_EVENT_BEGIN.plusSeconds(1),
            PRESENT,
            List.of(groupOne.getRef()),
            studentAxel.getRef(),
            studentAxel.getFirstName());
    assertEquals(
        axelAttendsIntegration.getId(), withAllFilters.getFirst().getEventParticipant().getId());

    var byStudentRef =
        api.getAllEventParticipants(
            null, null, null, null, null, null, null, studentFreddy.getRef(), null);
    assertEquals(
        List.of(freddyAttendsIntegration.getId()),
        byStudentRef.stream().map(a -> a.getEventParticipant().getId()).toList());
  }

  @Test
  void manager_create_event_with_is_online_ok() throws ApiException {
    var api = apiAs(managerToken);

    var createdOnline =
        api.crupdateEvents(
                List.of(aCreatableEvent(INTEGRATION).isOnline(true)), null, null, null, null)
            .getFirst();
    createdEventIds.add(createdOnline.getId());

    var createdOffline =
        api.crupdateEvents(
                List.of(aCreatableEvent(INTEGRATION).isOnline(false)), null, null, null, null)
            .getFirst();
    createdEventIds.add(createdOffline.getId());

    assertEquals(Boolean.TRUE, api.getEventById(createdOnline.getId()).getIsOnline());
    assertNotEquals(Boolean.TRUE, api.getEventById(createdOffline.getId()).getIsOnline());
  }

  @Test
  void manager_create_event_with_is_online_null_defaults_to_false() throws ApiException {
    var api = apiAs(managerToken);

    var created =
        api.crupdateEvents(List.of(aCreatableEvent(INTEGRATION)), null, null, null, null)
            .getFirst();
    createdEventIds.add(created.getId());

    assertNotEquals(
        Boolean.TRUE,
        api.getEventById(created.getId()).getIsOnline(),
        "Event should default to offline when isOnline is" + " null");
  }
}
