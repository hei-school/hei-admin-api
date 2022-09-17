package school.hei.haapi.integration;

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
import school.hei.haapi.endpoint.rest.api.PlacesApi;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = EventIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class EventIT {

  @MockBean
  private SentryConf sentryConf;

  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  public static Event event1(){
    Event event = new Event();
    event.setId("event1_id");
    event.setEventName("PROG2");
    event.setEventType("course");
    event.setStartDate(Instant.parse("2021-11-08T08:25:24.00Z"));
    event.setEndDate(Instant.parse("2021-11-08T12:25:24.00Z"));
    event.setResponsible("teacher1_id");
    event.setPlace("place1_id");
    return event;
  }


  public static Event event2(){
    Event event = new Event();
    event.setId("event2_id");
    event.setEventName("SYS2");
    event.setEventType("course");
    event.setStartDate(Instant.parse("2022-11-08T08:25:24.00Z"));
    event.setEndDate(Instant.parse("2022-11-08T12:25:24.00Z"));
    event.setResponsible("teacher2_id");
    event.setPlace("place2_id");
    return event;
  }

  public static Event someCreatableEvent1(){
    Event event = new Event()
        .id("event3_id")
        .eventName("MGT1")
        .eventType("course")
        .startDate(Instant.parse("2022-11-08T12:25:24.00Z"))
        .endDate(Instant.parse("2022-11-08T16:25:24.00Z"))
        .responsible("teacher1_id")
        .place("place1_id");
    return event;
  }
  public static Event someCreatableEvent2(){
    Event event = new Event()
        .id("event4_id")
        .eventName("WEB1")
        .eventType("course")
        .startDate(Instant.parse("2022-11-08T12:25:24.00Z"))
        .endDate(Instant.parse("2022-11-08T16:25:24.00Z"))
        .responsible("teacher2_id")
        .place("place2_id");
    return event;
  }
  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    List<Event> events = api.getEvents(1,2);

    assertTrue(events.contains(event1()));
    assertTrue(events.contains(event2()));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateEvents(List.of(someCreatableEvent1(), someCreatableEvent2())));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(TEACHER1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    List<Event> events = api.getEvents(0,2);

    assertTrue(events.contains(event1()));
    assertTrue(events.contains(event2()));
  }
  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    PlacesApi api = new PlacesApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdateEvents(List.of(someCreatableEvent1(), someCreatableEvent2())));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(MANAGER1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    List<Event> events = api.getEvents(0,2);

    assertTrue(events.contains(event1()));
    assertTrue(events.contains(event2()));
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    Event toCreate1 = someCreatableEvent1();
    Event toCreate2 = someCreatableEvent2();


    PlacesApi api = new PlacesApi(manager1Client);
    List<Event> created = api.createOrUpdateEvents(List.of(toCreate1, toCreate2));
    toCreate1.setId(created.get(1).getId());
    toCreate2.setId(created.get(2).getId());

    assertEquals(toCreate1, api.getEventById(toCreate2.getId()));
    assertTrue(isValidUUID(toCreate1.getId()));
    assertEquals(toCreate2, api.getEventById(toCreate2.getId()));
    assertTrue(isValidUUID(toCreate2.getId()));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
