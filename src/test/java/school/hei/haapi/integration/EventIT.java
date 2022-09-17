package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.EVENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.EventsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = EventIT.ContextInitializer.class)
@AutoConfigureMockMvc
class EventIT {

  @MockBean
  private SentryConf sentryConf;

  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  public static Event event1() {
    return new Event()
        .id("event1_id")
        .type("course")
        .place("place1_id")
        .startDatetime(Instant.parse("2022-11-08T08:25:24.00Z"))
        .endDatetime(Instant.parse("2022-11-08T08:25:24.00Z"))
        .responsible("teacher1_id");
  }

  public static List<Event> someCreatableEvents() {
    return List.of(
        new Event()
            .place("place2_id")
            .type("course")
            .endDatetime(Instant.parse("2022-08-23T10:22:22.00Z"))
            .startDatetime(Instant.parse("2022-08-23T10:22:22.00Z"))
            .responsible("teacher1_id")
    );
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void badtoken_read_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    EventsApi api = new EventsApi(anonymousClient);
    assertThrowsForbiddenException(() -> api.getEvents(1, 100));
  }

  @Test
  void badtoken_write_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    EventsApi api = new EventsApi(anonymousClient);
    assertThrowsForbiddenException(() -> api.createOrUpdateEvents(List.of()));
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    EventsApi api = new EventsApi(student1Client);
    Event actual1 = api.getEventById(EVENT1_ID);
    List<Event> actualCourses = api.getEvents(1, 100);

    assertEquals(event1(), actual1);
    assertTrue(actualCourses.contains(event1()));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    EventsApi api = new EventsApi(student1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateEvents(someCreatableEvents()));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    EventsApi api = new EventsApi(teacher1Client);
    assertThrowsForbiddenException(() ->
        api.createOrUpdateEvents(someCreatableEvents()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(MANAGER1_TOKEN);

    EventsApi api = new EventsApi(student1Client);
    Event actual1 = api.getEventById(EVENT1_ID);
    List<Event> actualEvents = api.getEvents(1, 100);

    assertEquals(event1(), actual1);
    assertTrue(actualEvents.contains(event1()));
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    List<Event> toCreate1 = someCreatableEvents();

    EventsApi api = new EventsApi(manager1Client);
    List<Event> created = api.createOrUpdateEvents(toCreate1);
    Event event = toCreate1.get(0);
    event.setId(created.get(0).getId());

    assertEquals(toCreate1, created);
    assertTrue(isValidUUID(created.get(0).getId()));
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(manager1Client);

    List<Event> toUpdate = api.createOrUpdateEvents(
        someCreatableEvents());
    Event event = toUpdate.get(0);
    event.setResponsible("teacher2_id");

    List<Event> updated = api.createOrUpdateEvents(List.of(event));

    assertEquals(toUpdate, updated);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
