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
import school.hei.haapi.endpoint.rest.api.EventsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.model.User;
import school.hei.haapi.endpoint.rest.model.Event.StatusEnum;
import school.hei.haapi.endpoint.rest.model.Place;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    public static User teacher1() {
        User teacher = new User();
        teacher.setId("teacher1_id");
        teacher.setFirstName("One");
        teacher.setLastName("Teacher");
        teacher.setEmail("test+teacher1@hei.school");
        teacher.setRef("TCR21001");
        teacher.setPhone("0322411125");
        teacher.setStatus(EnableStatus.ENABLED);
        teacher.setSex(User.SexEnum.F);
        teacher.setAddress("Adr 3");
        return teacher;
    }

    public static User manager1() {
        User manager = new User();
        manager.setId("manager1_id");
        manager.setFirstName("One");
        manager.setLastName("Manager");
        manager.setEmail("test+manager1@hei.school");
        manager.setRef("TCR21001");
        manager.setPhone("0322411125");
        manager.setStatus(EnableStatus.ENABLED);
        manager.setSex(User.SexEnum.F);
        manager.setAddress("Adr 3");
        return manager;
    }

    public static Place place1() {
        Place place = new Place();
        place.setId("place1_id");
        place.setName("class hei");
        return place;
    }

    public static Event event1() {
        Event event = new Event();
        event.setId("event1_id");
        event.setDescription("Some description");
        event.setEventType(Math.random() < 0.3 ? Event.EventTypeEnum.COURSES : Event.EventTypeEnum.EXAMS);
        event.setResponsible(teacher1());
        event.setStart(Instant.parse("2022-09-08T08:00:00.00Z"));
        event.setStart(Instant.parse("2022-09-08T10:30:00.00Z"));
        event.setStatus(Math.random() < 0.3 ? Event.StatusEnum.ACTIVE : Event.StatusEnum.CANCEL);
        event.setPlace(place1());
        return event;
    }

    public static Event event2() {
        Event event = new Event();
        event.setId("event2_id");
        event.setDescription("Some description");
        event.setEventType(Event.EventTypeEnum.COURSES);
        event.setResponsible(manager1());
        event.setStart(Instant.parse("2022-09-08T08:00:00.00Z"));
        event.setStart(Instant.parse("2022-09-08T10:30:00.00Z"));
        event.setStatus(Math.random() < 0.3 ? Event.StatusEnum.ACTIVE : Event.StatusEnum.CANCEL);
        event.setPlace(place1());
        return event;
    }

    public static Event someCreatableEvent() {
        Event event = new Event();
        event.setEventType(Event.EventTypeEnum.CONFERENCES);
        event.setDescription("some description");
        event.setResponsible(teacher1());
        event.setStatus(StatusEnum.CANCEL);
        event.setPlace(place1());
        return event;
    }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @Test
    void badtoken_read_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);

        EventsApi api = new EventsApi(anonymousClient);
        assertThrowsForbiddenException(api::getEvents);
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
        List<school.hei.haapi.endpoint.rest.model.Event> actualEvents = api.getEvents();

        assertEquals(event1(), actual1);
        assertTrue(actualEvents.contains(event1()));
        assertTrue(actualEvents.contains(event2()));
    }

    @Test
    void student_write_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        EventsApi api = new EventsApi(student1Client);
        assertThrowsForbiddenException(() -> api.createOrUpdateEvents(List.of()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

        EventsApi api = new EventsApi(teacher1Client);
        assertThrowsForbiddenException(() -> api.createOrUpdateEvents(List.of()));
    }

    @Test
    void manager_write_create_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        Event toCreate3 = someCreatableEvent();
        Event toCreate4 = someCreatableEvent();

        EventsApi api = new EventsApi(manager1Client);
        List<Event> created = api.createOrUpdateEvents(List.of(toCreate3, toCreate4));

        assertEquals(2, created.size());
        Event created3 = created.get(0);
        assertTrue(isValidUUID(created3.getId()));
        toCreate3.setId(created3.getId());
        assertNotNull(created3.getStatus());
        toCreate3.setStatus(created3.getStatus());
        //
        assertEquals(created3, toCreate3);
        Event created4 = created.get(0);
        assertTrue(isValidUUID(created4.getId()));
        toCreate4.setId(created4.getId());
        assertNotNull(created4.getStatus());
        toCreate4.setStatus(created4.getStatus());
        assertEquals(created4, toCreate3);
    }

    @Test
    void manager_write_update_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        EventsApi api = new EventsApi(manager1Client);
        List<Event> toUpdate = api.createOrUpdateEvents(List.of(
                someCreatableEvent(),
                someCreatableEvent()));
        Event toUpdate0 = toUpdate.get(0);
        toUpdate0.setEventType(Event.EventTypeEnum.EXAMS);
        Event toUpdate1 = toUpdate.get(1);
        toUpdate1.setEventType(Event.EventTypeEnum.COURSES);

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
