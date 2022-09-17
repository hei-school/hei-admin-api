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
import school.hei.haapi.endpoint.rest.api.EventApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateEventParticipant;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

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
        EventParticipant eventParticipant = new EventParticipant();
        eventParticipant.setId("event_participant1_id");
        eventParticipant.setStatus(EventParticipant.StatusEnum.EXPECTED);
        eventParticipant.setUserParticipantId("student1_id");
        eventParticipant.setEventId("event1_id");
        return eventParticipant;
    }

    public static CreateEventParticipant someCreatableCreateEventParticipant() {
        CreateEventParticipant createEventParticipant = new CreateEventParticipant();
        createEventParticipant.setUserParticipantId("student3_id");
        return createEventParticipant;
    }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void badtoken_read_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);
        EventApi api = new EventApi(anonymousClient);
        assertThrowsForbiddenException(() -> api.getEventEventParticipants(EVENT1_ID, 0, 2, "EXPECTED"));
    }

    @Test
    void badtoken_write_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);

        EventApi api = new EventApi(anonymousClient);
        assertThrowsForbiddenException(() -> api.createEventEventParticipants(EVENT1_ID, List.of()));
    }

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        EventApi api = new EventApi(student1Client);
        EventParticipant actual1 = api.getEventEventParticipantById(EVENT1_ID, EVENT_PARTICIPANT1_ID);
        List<EventParticipant> actualEventParticipants = api.getEventParticipants(null, null, null);

        assertEquals(eventParticipant1(), actual1);
        assertTrue(actualEventParticipants.contains(eventParticipant1()));
    }

    @Test
    void student_write_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        EventApi api = new EventApi(student1Client);
        assertThrowsForbiddenException(() -> api.createEventEventParticipants(EVENT1_ID, List.of()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        EventApi api = new EventApi(teacher1Client);
        assertThrowsForbiddenException(() -> api.createEventEventParticipants(EVENT1_ID, List.of()));
    }

    @Test
    void manager_write_create_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        CreateEventParticipant toCreate3 = someCreatableCreateEventParticipant();
        CreateEventParticipant toCreate4 = someCreatableCreateEventParticipant();

        EventApi api = new EventApi(manager1Client);
        List<EventParticipant> created = api.createEventEventParticipants(EVENT2_ID, List.of(toCreate3, toCreate4));

        EventParticipant created3 = new EventParticipant();
        created3.setId(created.get(0).getId());
        created3.setEventId(EVENT2_ID);
        created3.setStatus(EventParticipant.StatusEnum.EXPECTED);
        created3.setUserParticipantId(toCreate3.getUserParticipantId());

        EventParticipant created4 = new EventParticipant();
        created4.setId(created.get(1).getId());
        created4.setEventId(EVENT2_ID);
        created4.setStatus(EventParticipant.StatusEnum.EXPECTED);
        created4.setUserParticipantId(toCreate4.getUserParticipantId());

        assertEquals(2, created.size());
        assertTrue(isValidUUID(created.get(0).getId()));
        assertEquals(created.get(0), created3);
        //
        assertTrue(isValidUUID(created.get(1).getId()));
        assertEquals(created.get(1), created4);
    }

    @Test
    void manager_write_update_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        EventApi api = new EventApi(manager1Client);
        List<EventParticipant> toUpdate = api.createEventEventParticipants(EVENT2_ID, List.of(someCreatableCreateEventParticipant()));
        EventParticipant toUpdate0 = toUpdate.get(0);
        toUpdate0.setStatus(EventParticipant.StatusEnum.HERE);

        List<EventParticipant> updated = api.updateEventEventParticipants(EVENT2_ID, toUpdate);

        assertEquals(1, updated.size());
        assertTrue(updated.contains(toUpdate0));
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
