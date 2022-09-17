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
import school.hei.haapi.endpoint.rest.model.CreateEventParticipants;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
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
@ContextConfiguration(initializers = EventParticipantIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class EventParticipantIT {

  @MockBean
  private SentryConf sentryConf;

  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  public static EventParticipant eventParticipant1(){
    EventParticipant eventParticipant = new EventParticipant();
    eventParticipant.setId("eventParticipant1_id");
    eventParticipant.setParticipantRef("student1_id");
    eventParticipant.setEventId("event1_id");
    eventParticipant.setStatus(EventParticipant.StatusEnum.EXPECTED);
    return eventParticipant;
  }


  public static EventParticipant eventParticipant2(){
    EventParticipant eventParticipant = new EventParticipant();
    eventParticipant.setId("eventParticipant2_id");
    eventParticipant.setParticipantRef("student1_id");
    eventParticipant.setEventId("event1_id");
    eventParticipant.setStatus(EventParticipant.StatusEnum.EXPECTED);
    return eventParticipant;
  }

  public static CreateEventParticipants someCreatableEventParticipant1(){
   return new CreateEventParticipants()
       .eventId("event1_id")
       .groupId("group1_id");
  }

  public static EventParticipant createdParticipants() {
    return new EventParticipant()
        .id("student2_id")
        .participantRef("student1_id")
        .eventId("event1_id")
        .status(EventParticipant.StatusEnum.EXPECTED)
        ;
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    List<EventParticipant> eventParticipants = api.getParticipants(1,10,"event1_id"
    , "EXPECTED", "student1_id");

    assertTrue(eventParticipants.contains(eventParticipant1()));
    assertTrue(eventParticipants.contains(eventParticipant1()));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    assertThrowsForbiddenException(() -> api.createEventParticipants("event1_id", List.of(
        someCreatableEventParticipant1())
    ));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(TEACHER1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    List<EventParticipant> eventParticipants = api.getParticipants(1,10,"event1_id"
        , "EXPECTED", "student1_id");

    assertTrue(eventParticipants.contains(eventParticipant1()));
    assertTrue(eventParticipants.contains(eventParticipant1()));
  }
  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    PlacesApi api = new PlacesApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.createEventParticipants("event1_id", List.of(
        someCreatableEventParticipant1())
    ));

  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(MANAGER1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    List<EventParticipant> eventParticipants = api.getParticipants(1,10,"event1_id"
        , "EXPECTED", "student1_id");

    assertTrue(eventParticipants.contains(eventParticipant1()));
    assertTrue(eventParticipants.contains(eventParticipant1()));
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    CreateEventParticipants toCreate1 = someCreatableEventParticipant1();


    PlacesApi api = new PlacesApi(manager1Client);
    List<EventParticipant> created = api.createEventParticipants("eventid_1", List.of(
        someCreatableEventParticipant1()
    ));
    EventParticipant eventParticipant = created.get(0);
    EventParticipant expected1 = createdParticipants();
    expected1.setId(eventParticipant.getId());

    assertEquals(List.of(eventParticipant), List.of(expected1));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
