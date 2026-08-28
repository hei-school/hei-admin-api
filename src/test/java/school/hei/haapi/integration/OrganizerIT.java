package school.hei.haapi.integration;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.PRESENT;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCreatableEvent;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestFiles.uploadProfilePicture;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.testData.EventTestData.aParticipant;
import static school.hei.haapi.integration.testData.EventTestData.anEvent;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.OrganizerTestData.organizerDoe;
import static school.hei.haapi.integration.testData.OrganizerTestData.organizerSmith;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.EventsApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.endpoint.rest.model.Organizer;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class OrganizerIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private EventRepository eventRepository;
  @Autowired private EventParticipantRepository eventParticipantRepository;
  @Autowired private GroupRepository groupRepository;

  private User organizerOne;
  private User organizerTwo;
  private User adminUser;
  private User studentAxel;
  private User teacherToky;
  private Group groupG1;
  private Event integrationDay;
  private EventParticipant axelParticipation;

  private String organizerOneToken;
  private String organizerTwoToken;
  private String adminToken;

  private void setUpTestData() {
    organizerOne = userRepository.save(organizerSmith());
    organizerTwo = userRepository.save(organizerDoe());
    adminUser = userRepository.save(adminMialy());
    studentAxel = userRepository.save(axel());
    teacherToky = userRepository.save(toky());
    groupG1 = groupRepository.save(g1());

    integrationDay =
        eventRepository.save(
            anEvent(
                organizerOne,
                EventType.INTEGRATION,
                "Integration Day",
                Instant.parse("2022-12-08T08:00:00.00Z"),
                Instant.parse("2022-12-08T12:00:00.00Z")));
    axelParticipation =
        eventParticipantRepository.save(
            aParticipant(integrationDay, studentAxel, groupG1, PRESENT));
  }

  @BeforeEach
  public void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();

    organizerOneToken = tokenFor(casdoorAuthServiceMock, organizerOne);
    organizerTwoToken = tokenFor(casdoorAuthServiceMock, organizerTwo);
    adminToken = tokenFor(casdoorAuthServiceMock, adminUser);
  }

  @AfterEach
  void tearDown() {
    eventParticipantRepository.deleteById(axelParticipation.getId());
    eventRepository.deleteById(integrationDay.getId());
    groupRepository.deleteById(groupG1.getId());
    userRepository.deleteAll(
        List.of(organizerOne, organizerTwo, adminUser, studentAxel, teacherToky));
  }

  private UsersApi usersApiAs(String token) {
    return new UsersApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @Test
  void organizer_update_own_profile_picture() throws IOException, InterruptedException {
    var response =
        uploadProfilePicture(localPort, organizerOneToken, organizerOne.getId(), "organizers");

    var organizer = objectMapper.readValue(response.body(), Organizer.class);

    assertEquals(200, response.statusCode());
    assertEquals(organizerOne.getRef(), organizer.getRef());
  }

  @Test
  void read_student_ko() {
    var api = usersApiAs(organizerTwoToken);

    assertThrowsForbiddenException(() -> api.getStudentById(studentAxel.getId()));
    assertThrowsForbiddenException(
        () -> api.getStudents(1, 20, null, null, null, null, null, null, null, null, null));
  }

  @Test
  void read_teacher_ko() {
    var api = usersApiAs(organizerOneToken);

    assertThrowsForbiddenException(() -> api.getTeacherById(teacherToky.getId()));
    assertThrowsForbiddenException(() -> api.getTeachers(1, 20, null, null, null, null, null));
  }

  @Test
  void read_events_ok() throws ApiException {
    var api = new EventsApi(anApiClient(organizerOneToken));

    var events = api.getEvents(1, 100, null, null, null, null, null, null, null);

    assertFalse(events.isEmpty());
    assertTrue(events.stream().anyMatch(e -> integrationDay.getId().equals(e.getId())));
  }

  @Test
  void manipulate_events_ok() throws ApiException {
    var api = new EventsApi(anApiClient(organizerOneToken));
    var createEvent =
        someCreatableEvent(
            EventType.EXAM, organizerOne.getId(), Instant.now(), Instant.now().plus(1, HOURS));

    var events = api.crupdateEvents(List.of(createEvent), null, null, null, null);
    var newEvent = events.getFirst();
    assertEquals(createEvent.getTitle(), newEvent.getTitle());

    api.deleteEventById(newEvent.getId());
    assertThrows(ApiException.class, () -> api.getEventById(newEvent.getId()));
  }

  @Test
  void admin_all_organizers_ok() throws ApiException {
    var organizers = usersApiAs(adminToken).getOrganizers(1, 100, null, null, null, null, null);

    assertTrue(organizers.stream().anyMatch(o -> organizerOne.getId().equals(o.getId())));
    assertTrue(organizers.stream().anyMatch(o -> organizerTwo.getId().equals(o.getId())));
  }

  @Test
  void admin_organizer_by_id_ok() throws ApiException {
    var organizer = usersApiAs(adminToken).getOrganizerById(organizerOne.getId());

    assertEquals(organizerOne.getId(), organizer.getId());
    assertEquals(organizerOne.getRef(), organizer.getRef());
  }

  @Test
  void organizer_access_to_its_own_account_ok() throws ApiException {
    var organizer = usersApiAs(organizerOneToken).getOrganizerById(organizerOne.getId());

    assertEquals(organizerOne.getId(), organizer.getId());
  }

  @Test
  void admin_modify_organizers_ok() throws ApiException {
    var api = usersApiAs(adminToken);
    var organizer = api.getOrganizerById(organizerOne.getId());
    var originalFirstName = organizer.getFirstName();

    var renamed = api.crupdateOrganizers(List.of(organizer.firstName("firstName()")));
    assertEquals("firstName()", renamed.getFirst().getFirstName());

    var restored = api.crupdateOrganizers(List.of(organizer.firstName(originalFirstName)));
    assertEquals(originalFirstName, restored.getFirst().getFirstName());
  }

  @Test
  void get_eventParticipants_ok() throws ApiException {
    var api = new EventsApi(anApiClient(organizerOneToken));

    var eventParticipants =
        api.getEventParticipants(integrationDay.getId(), 1, 10, null, null, null, null);

    assertFalse(eventParticipants.isEmpty());
    assertTrue(
        eventParticipants.stream().anyMatch(p -> axelParticipation.getId().equals(p.getId())));
  }
}
