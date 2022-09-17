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
import school.hei.haapi.endpoint.rest.api.EventApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.EVENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER_ID;
import static school.hei.haapi.integration.conf.TestUtils.PLACE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.PLACE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_ID;
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

    public static Event event1() {
        Event event = new Event();
        event.setId("event1_id");
        event.setName("Name of event one");
        event.setRef("EVT21001");
        event.setStartingHours(Instant.parse("2022-09-11T12:00:00.00Z"));
        event.setEndingHours(Instant.parse("2022-09-11T14:00:00.00Z"));
        event.setUserManagerId(MANAGER_ID);
        event.setPlaceId(PLACE1_ID);
        return event;
    }

    public static Event event2() {
        Event event = new Event();
        event.setId("event2_id");
        event.setName("Name of event two");
        event.setRef("EVT21002");
        event.setStartingHours(Instant.parse("2022-10-11T12:00:00.00Z"));
        event.setEndingHours(Instant.parse("2022-09-11T14:00:00.00Z"));
        event.setUserManagerId(MANAGER_ID);
        event.setPlaceId(PLACE1_ID);
        return event;
    }

    public static Event someCreatableEvent() {
        Event event = new Event();
        event.setName("some name");
        event.setRef("EVT-" + randomUUID());
        event.setStartingHours(Instant.parse("2022-10-11T12:00:00.00Z"));
        event.setEndingHours(Instant.parse("2022-09-11T14:00:00.00Z"));
        event.setUserManagerId(TEACHER1_ID);
        event.setPlaceId(PLACE2_ID);
        return event;
    }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void badtoken_read_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);
        EventApi api = new EventApi(anonymousClient);
        assertThrowsForbiddenException(() -> api.getEvents(PLACE1_ID));
    }

    @Test
    void badtoken_write_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);

        EventApi api = new EventApi(anonymousClient);
        assertThrowsForbiddenException(() -> api.createOrUpdateEvents(List.of()));
    }
    
    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        EventApi api = new EventApi(student1Client);
        Event actual1 = api.getEventById(EVENT1_ID);
        List<Event> actualEvents = api.getEvents(null);

        assertEquals(event1(), actual1);
        assertTrue(actualEvents.contains(event1()));
        assertTrue(actualEvents.contains(event2()));
    }

    @Test
    void student_write_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        EventApi api = new EventApi(student1Client);
        assertThrowsForbiddenException(() -> api.createOrUpdatePlaces(List.of()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        EventApi api = new EventApi(teacher1Client);
        assertThrowsForbiddenException(() -> api.createOrUpdateEvents(List.of()));
    }

    @Test
    void manager_write_create_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        Event toCreate3 = someCreatableEvent();
        Event toCreate4 = someCreatableEvent();

        EventApi api = new EventApi(manager1Client);
        List<Event> created = api.createOrUpdateEvents(List.of(toCreate3, toCreate4));

        assertEquals(2, created.size());
        assertTrue(isValidUUID(created.get(0).getId()));
        toCreate3.setId(created.get(0).getId());
        assertEquals(created.get(0), toCreate3);
        //
        assertTrue(isValidUUID(created.get(1).getId()));
        toCreate4.setId(created.get(1).getId());
        assertEquals(created.get(1), toCreate4);
    }

    @Test
    void manager_write_update_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        EventApi api = new EventApi(manager1Client);
        List<Event> toUpdate = api.createOrUpdateEvents(List.of(
                someCreatableEvent(),
                someCreatableEvent()));
        Event toUpdate0 = toUpdate.get(0);
        toUpdate0.setName("A new name zero");
        Event toUpdate1 = toUpdate.get(1);
        toUpdate1.setName("A new name one");

        List<Event> updated = api.createOrUpdateEvents(toUpdate);

        assertEquals(2, updated.size());
        assertTrue(updated.contains(toUpdate0));
        assertTrue(updated.contains(toUpdate1));
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
