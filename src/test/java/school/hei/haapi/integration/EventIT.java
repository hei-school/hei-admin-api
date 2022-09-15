package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.PlaceApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.EVENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EVENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.PLACE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.PLACE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER2_ID;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
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
        return TestUtils.anApiClient(token, EventIT.ContextInitializer.SERVER_PORT);
    }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }

    static Event event1() {
        return new Event()
                .id(EVENT1_ID)
                .courseId(COURSE1_ID)
                .placeId(PLACE1_ID)
                .supervisorId(TEACHER1_ID)
                .title("title1")
                .type("type1")
                .startingTime(Instant.parse("2023-12-08T08:25:24.00Z"))
                .endingTime(Instant.parse("2023-12-09T08:25:24.00Z"));
    }

    static Event event2() {
        return new Event()
                .id(EVENT2_ID)
                .courseId(COURSE2_ID)
                .placeId(PLACE2_ID)
                .supervisorId(TEACHER2_ID)
                .type("type2")
                .title("title2")
                .startingTime(Instant.parse("2025-12-08T08:25:24.00Z"))
                .endingTime(Instant.parse("2026-12-08T09:25:24.00Z"));
    }


    static Event event3() {
        return new Event()
                .id(EVENT1_ID)
                .placeId(PLACE1_ID)
                .supervisorId(TEACHER1_ID)
                .type("conference")
                .startingTime(Instant.now())
                .endingTime(Instant.now().plusMillis(3600))
                .title("TITLE1");
    }

    @Test
    void teacher_read_ok() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        PlaceApi api = new PlaceApi(teacher1Client);

        Event actualEvent = api.getEventById(EVENT1_ID);
        List<Event> actual = api.getEvents(1, 5, null);

        assertEquals(event1(), actualEvent);
        assertTrue(actual.contains(event1()));
    }

    @Test
    void manager_read_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PlaceApi api = new PlaceApi(manager1Client);

        Event actualEvent = api.getEventById(EVENT1_ID);
        List<Event> actual = api.getEvents(1, 5, null);

        assertEquals(event1(), actualEvent);
        assertTrue(actual.contains(event1()));
    }

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(MANAGER1_TOKEN);
        PlaceApi api = new PlaceApi(student1Client);

        Event actualEvent = api.getEventById(EVENT1_ID);
        List<Event> actual = api.getEvents(1, 5, null);

        assertEquals(event1(), actualEvent);
        assertTrue(actual.contains(event1()));
    }

     @Test
     void manger_write_ok() throws ApiException {
         ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
         PlaceApi api = new PlaceApi(manager1Client);

         List<Event> actual = api.createEvents(List.of(event1()));

         List<Event> expected = api.getEvents(1, 5, PLACE1_ID);
         assertTrue(expected.containsAll(actual));
     }
}
