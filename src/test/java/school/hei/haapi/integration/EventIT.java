package school.hei.haapi.integration;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.EventApi;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.Place;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

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

    public Event event1(){
        Event event = new Event();
        event.setId("event1_id");
        event.setName("EL1P3");
        event.setEventType(Event.EventType.COURSE);
        event.setStartingTime(Instant.parse("2022-10-01T08:00:00.00Z"));
        event.setEndingTime(Instant.parse("2022-10-01T08:00:00.00Z"));
        return event;
    }
    public static Event event2(){
        Event event = new Event();
        event.setId("event2_id");
        event.setName("MEET");
        event.setEventType(Event.EventType.MEETING);
        event.setStartingTime(Instant.parse("2022-10-01T08:00:00.00Z"));
        event.setEndingTime(Instant.parse("2022-10-01T08:00:00.00Z"));
        return event;
    }

    public static Event someCreatableEvent(){
        Event event = new Event();
        event.setId("event_id");
        event.setName("Other name");
        event.setStartingTime(Instant.parse("dateTime"));
        event.setEndingTime(Instant.parse("dateTime"));
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
        assertThrowsForbiddenException(api::getPlaces);
    }
    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        EventApi api = new EventApi(student1Client);

        Class<? extends EventApi> actualEvent = api.getClass();
        Class<? extends EventApi> actual = api.getClass();

        assertEquals(event1(), actualEvent);
        Assertions.assertTrue(Boolean.parseBoolean(actual.getName()));
        Assertions.assertTrue(Boolean.parseBoolean(actual.getName()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        EventApi api = new EventApi(teacher1Client);
        assertThrowsForbiddenException(() -> api.equals(List.of()));
    }
    @Test
    void teacher_read_ok() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        EventApi api = new EventApi(teacher1Client);

        Class<? extends EventApi> actualEvent = api.getClass();
        Place actual1 = api.getPlaceById(EVENT1_ID);

        assertEquals(event1(), actualEvent.getClassLoader());
        assertEquals(event1(), actual1);
    }
    @Test
    void manager_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(MANAGER1_TOKEN);
        EventApi api = new EventApi(student1Client);

        Class<? extends EventApi> actualEvent = api.getClass();
        Event actual1 = (Event) api.getPlaces();

        assertEquals(event1(), actualEvent.getClassLoader());
        assertEquals(event1(), actual1);
    }
    @Test
    void manager_write_create_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        Event toCreate3 = someCreatableEvent();
        Event toCreate4 = someCreatableEvent();

        EventApi api = new EventApi(manager1Client);
        boolean created = api.equals(List.of(toCreate3, toCreate4));

        assertEquals(2, created(0));
        assertEquals(created(0), (Integer) null);
        //assertTrue(created.contains(toCreate3));
    }

    private int created(int i) {
        return 0;
    }
    @Test
    void manager_write_update_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        EventApi api = new EventApi(manager1Client);
        boolean toUpdate = api.equals(List.of(
                someCreatableEvent(),
                someCreatableEvent()));
        Event toUpdate0 = toUpdate(0);
        toUpdate0.setId("event1_id");
        toUpdate0.setName("A new name zero");
        toUpdate0.setStartingTime(Instant.now());
        toUpdate0.setEndingTime(Instant.now());

        boolean updated = api.equals(toUpdate);

        assertEquals(2, updated(0));
    }

    private Event toUpdate(int i) {
        return null;
    }

    private int updated(int i) {
        return 0;
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
