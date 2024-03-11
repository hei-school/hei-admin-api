package school.hei.haapi.integration;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.EventsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.createEventCourse1;
import static school.hei.haapi.integration.conf.TestUtils.event1;
import static school.hei.haapi.integration.conf.TestUtils.event2;
import static school.hei.haapi.integration.conf.TestUtils.expectedCourseEventCreated;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = EventIT.ContextInitializer.class)
@AutoConfigureMockMvc
@Slf4j
public class EventIT extends MockedThirdParties {

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, EventIT.ContextInitializer.SERVER_PORT);
    }

    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
        setUpS3Service(fileService, student1());
    }

    @Test
    void manager_create_event_ok() throws ApiException {
        ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
        EventsApi api = new EventsApi(apiClient);
        log.info(createEventCourse1().getId());
        List<Event> actual = api.crupdateEvents(List.of(createEventCourse1()));
        log.info(actual.get(0).getId());
        assertEquals(1, actual.size());
        assertEquals(expectedCourseEventCreated().getType(), actual.get(0).getType());

    }

    @Test
    void manager_read_event_ok() throws ApiException{
        ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
        EventsApi api = new EventsApi(apiClient);

        List<Event> actual = api.getEvents(1,15, null, null, null, null);

        assertTrue(actual.contains(event1()));
        assertTrue(actual.contains(event2()));

    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }



}
