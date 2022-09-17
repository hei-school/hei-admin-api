package school.hei.haapi.integration;

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
import school.hei.haapi.endpoint.rest.model.CreateEventParticipants;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.EVENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EVENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.EVENTP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EVENTP2_ID;
import static school.hei.haapi.integration.conf.TestUtils.GROUP2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
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

  public static EventParticipant eventParticipant1() {
    return new EventParticipant()
        .id(EVENTP1_ID)
        .studentId(STUDENT1_ID)
        .eventId(EVENT1_ID)
        .status(EventParticipant.StatusEnum.EXPECTED);
  }

  public static EventParticipant eventParticipant2() {
    return new EventParticipant()
        .id(EVENTP2_ID)
        .studentId(STUDENT2_ID)
        .eventId(EVENT2_ID)
        .status(EventParticipant.StatusEnum.EXPECTED);
  }

  public static EventParticipant eventParticipant3() {
    return new EventParticipant()
        .id(EVENTP2_ID)
        .studentId("student3_id")
        .eventId(EVENT1_ID)
        .status(EventParticipant.StatusEnum.EXPECTED);
  }

  public static CreateEventParticipants aCreatableGroupEventParticipant() {
    return new CreateEventParticipants()
        .eventId(EVENT1_ID)
        .groupId(GROUP2_ID);
  }

  public static EventParticipant createdParticipants() {
    return new EventParticipant()
        .id(EVENTP2_ID)
        .studentId(STUDENT2_ID)
        .eventId(EVENT2_ID)
        .status(EventParticipant.StatusEnum.EXPECTED)
        ;
  }

  public static EventParticipant aCreatableEventParticipant() {
    return new EventParticipant()
        .id(EVENTP2_ID)
        .studentId(STUDENT2_ID)
        .eventId(EVENT2_ID)
        .status(EventParticipant.StatusEnum.EXPECTED);
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void badtoken_read_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    EventsApi api = new EventsApi(anonymousClient);
    assertThrowsForbiddenException(() -> api.getParticipants(1, "event1_id", 10, null, null));
  }

  @Test
  void badtoken_write_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    EventsApi api = new EventsApi(anonymousClient);
    assertThrowsForbiddenException(
        () -> api.createEventParticipants("event1_id", List.of(aCreatableGroupEventParticipant())));
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    EventsApi api = new EventsApi(student1Client);
    EventParticipant actual1 = api.getParticipantById(EVENT1_ID, EVENTP1_ID);
    List<EventParticipant> actualParticipants =
        api.getParticipants(1, EVENT1_ID, 100, null, null);

    assertEquals(eventParticipant1(), actual1);
    assertTrue(actualParticipants.contains(eventParticipant1()));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    EventsApi api = new EventsApi(student1Client);
    assertThrowsForbiddenException(
        () -> api.createEventParticipants("event1_id", List.of(aCreatableGroupEventParticipant())));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    EventsApi api = new EventsApi(teacher1Client);
    assertThrowsForbiddenException(
        () -> api.createEventParticipants("event1_id", List.of(aCreatableGroupEventParticipant())));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(MANAGER1_TOKEN);

    EventsApi api = new EventsApi(student1Client);
    EventParticipant actual1 = api.getParticipantById(EVENT1_ID, EVENTP1_ID);
    List<EventParticipant> actualParticipants =
        api.getParticipants(1, EVENT1_ID, 100, null, null);

    assertEquals(eventParticipant1(), actual1);
    assertTrue(actualParticipants.contains(eventParticipant1()));
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(manager1Client);

    List<EventParticipant> created =
        api.createEventParticipants(EVENT2_ID, List.of(aCreatableGroupEventParticipant()));
    EventParticipant participant1 = created.get(0);
    EventParticipant expected1 = createdParticipants();
    expected1.setId(participant1.getId());

    assertEquals(List.of(expected1), List.of(participant1));
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    EventsApi api = new EventsApi(manager1Client);

    List<EventParticipant> created =
        api.createOrUpdateParticipant("event2_id", List.of(aCreatableEventParticipant()));
    EventParticipant participant1 = created.get(0);
    participant1.setStatus(EventParticipant.StatusEnum.HERE);
    List<EventParticipant> updated =
        api.createOrUpdateParticipant("event2_id", List.of(participant1));

    EventParticipant expected1 = eventParticipant2();
    expected1.setStatus(EventParticipant.StatusEnum.HERE);
    expected1.setId(updated.get(0).getId());
    assertEquals(List.of(expected1), updated);
  }


  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
