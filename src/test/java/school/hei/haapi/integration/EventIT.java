package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.PlacesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.EVENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EVENT2_ID;
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



    public static Event Event1() {
        Event Event = new Event();
        Event.setId("event1_id");
        Event.setName("Examen prog2");
        Event.setResponsibleId("teacher1_id");
        Event.setPlaceId("place1_id");
        Event.setType("EXAM");
        Event.setStatus("CANCELLED");
        Event.setStartEvent(Instant.parse("2022-11-08T08:25:24.00Z"));
        Event.setEndEvent(Instant.parse("2022-11-08T09:25:24.00Z"));
        return Event;
    }

    public static Event Event2() {
        Event Event = new Event();
        Event.setId("event2_id");
        Event.setName("conference l1");
        Event.setResponsibleId("manager1_id");
        Event.setPlaceId("place2_id");
        Event.setType("MEETING");
        Event.setStatus("ONGOING");
        Event.setStartEvent(Instant.parse("2022-11-08T08:25:24.00Z"));
        Event.setEndEvent(Instant.parse("2022-11-08T09:25:24.00Z"));
        return Event;
    }


    public static Event someCreatableEvent(){
        Event Event = new Event();
        Event.setId("EVT-" + randomUUID());
        Event.setName("Some name");
        Event.setResponsibleId("teacher3_id");
        Event.setPlaceId("place1_id");
        Event.setType("MEETING");
        Event.setStatus("ONGOING");
        Event.setStartEvent(Instant.parse("2022-11-08T08:25:24.00Z"));
        Event.setEndEvent(Instant.parse("2022-11-08T09:25:24.00Z"));
        return Event;
    }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void badtoken_read_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);

        PlacesApi api = new PlacesApi(anonymousClient);
        assertThrowsForbiddenException(() -> api.createOrUpdateEvents(List.of()));
    }


    @Test
    void badtoken_write_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);
        PlacesApi api = new PlacesApi(anonymousClient);
        assertThrowsForbiddenException(() -> api.createOrUpdateEvents(List.of()));
    }

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        PlacesApi api = new PlacesApi(student1Client);
        Event actual1 = api.getEventById(EVENT1_ID);
        List<Event> actualEvents = api.getEvents(1, 20, null);

        assertEquals(Event1(), actual1);
        assertTrue(actualEvents.contains(Event1()));
        assertTrue(actualEvents.contains(Event2()));
    }

    @Test
    void student_write_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        PlacesApi api = new PlacesApi(student1Client);
        assertThrowsForbiddenException(() -> api.createOrUpdateEvents(List.of()));
    }

    @Test
    void teacher_read_ok() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        PlacesApi api = new PlacesApi(teacher1Client);
        Event actual2 = api.getEventById(EVENT2_ID);

        List<Event> actualEvents = api.getEvents(1, 20, null);

        assertEquals(Event2(), actual2);
        assertTrue(actualEvents.contains(Event1()));
        assertTrue(actualEvents.contains(Event2()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        PlacesApi api = new PlacesApi(teacher1Client);
        assertThrowsForbiddenException(() -> api.createOrUpdateEvents(List.of()));
    }

    @Test
    void manager_write_create_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PlacesApi api = new PlacesApi(manager1Client);
        Event toCreate = someCreatableEvent();

        List<Event> created = api.createOrUpdateEvents(List.of(toCreate));

        assertEquals(1, created.size());
        Event created0 = created.get(0);
        assertTrue(isValidUUID(created0.getId()));
        toCreate.setId(created0.getId());
        assertEquals(toCreate, created0);
    }

    @Test
    void manager_write_update_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PlacesApi api = new PlacesApi(manager1Client);
        List<Event> toUpdate = (api.createOrUpdateEvents(List.of(someCreatableEvent(), someCreatableEvent())));
        Event toUpdate3 = toUpdate.get(0);
        toUpdate3.setName("A new name 3");
        Event toUpdate4 = toUpdate.get(1);
        toUpdate4.setName("A new name 4");
        List<Event> updated = (api.createOrUpdateEvents(List.of(toUpdate.get(0), toUpdate.get(1))));
        assertEquals(2, updated.size());
        assertTrue(updated.contains(toUpdate3));
        assertTrue(updated.contains(toUpdate4));
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
