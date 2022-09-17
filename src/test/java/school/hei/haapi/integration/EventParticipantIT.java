package school.hei.haapi.integration;

import com.github.javafaker.Faker;
import java.util.List;
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
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.endpoint.rest.model.User;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.EventParticipant.StatusEnum.*;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = EventParticipantIT.ContextInitializer.class)
@AutoConfigureMockMvc
class EventParticipantIT {

    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    @MockBean
    private EventBridgeClient eventBridgeClientMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
    }

    public static school.hei.haapi.endpoint.rest.model.EventParticipant eventParticipant1() {
        EventParticipant eventParticipant = new school.hei.haapi.endpoint.rest.model.EventParticipant();
        eventParticipant.setId("eventParticipant1_id");
        eventParticipant.setUser(new User());
        eventParticipant.setEvent(EventIT.event1());
        eventParticipant.setStatus(EXPECTED);
        return eventParticipant;
    }

    public static school.hei.haapi.endpoint.rest.model.EventParticipant eventParticipant2() {
        school.hei.haapi.endpoint.rest.model.EventParticipant eventParticipant = new school.hei.haapi.endpoint.rest.model.EventParticipant();
        eventParticipant.setId("eventParticipant2_id");
        eventParticipant.setUser(new User());
        eventParticipant.setEvent(EventIT.event2());
        eventParticipant.setStatus(MISSING);
        return eventParticipant;
    }

    public static EventParticipant someCreatableEventParticipant() {
        EventParticipant eventParticipant = new EventParticipant();
        Faker faker = new Faker();
        eventParticipant.setId("EP_"+ faker.number().toString());
        eventParticipant.setEvent(EventIT.someCreatableEvent());
        eventParticipant.setUser(new User());
        return eventParticipant;
    }

    @BeforeEach
    public void setUp() {
        setUpCognito(cognitoComponentMock);
        setUpEventBridge(eventBridgeClientMock);
    }

    @Test
    void student_read_ko() {
        ApiClient eventParticipant1Client = anApiClient(STUDENT1_TOKEN);
        PlaceApi api = new PlaceApi(eventParticipant1Client);

        assertThrowsForbiddenException(
                () -> api.getEventParticipants("event1_id"));
    }

    @Test
    void teacher_read_ok() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        PlaceApi api = new PlaceApi(teacher1Client);

        List<school.hei.haapi.endpoint.rest.model.EventParticipant> actualEventParticipants = api.getEventParticipants("event1_id");

        assertTrue(actualEventParticipants.contains(eventParticipant1()));
        assertTrue(actualEventParticipants.contains(eventParticipant2()));
    }

    @Test
    void student_write_ko() {
        ApiClient eventParticipant1Client = anApiClient(STUDENT1_TOKEN);
        PlaceApi api = new PlaceApi(eventParticipant1Client);

        assertThrowsForbiddenException(() -> api.updateEventParticipants("event1_id",List.of()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        PlaceApi api = new PlaceApi(teacher1Client);

        assertThrowsForbiddenException(() -> api.updateEventParticipants("event1_id",List.of()));
    }

    @Test
    void manager_read_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PlaceApi api = new PlaceApi(manager1Client);

        List<school.hei.haapi.endpoint.rest.model.EventParticipant> actualEventParticipants = api.getEventParticipants("event1_id");

        assertTrue(actualEventParticipants.contains(eventParticipant1()));
        assertTrue(actualEventParticipants.contains(eventParticipant2()));
    }

    @Test
    void manager_write_update_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PlaceApi api = new PlaceApi(manager1Client);

        List<school.hei.haapi.endpoint.rest.model.EventParticipant> toUpdate =
                api.updateEventParticipants("event1_id",List.of(someCreatableEventParticipant(), someCreatableEventParticipant()));
        school.hei.haapi.endpoint.rest.model.EventParticipant toUpdate0 = toUpdate.get(0);
        toUpdate0.setId("A new id zero");
        school.hei.haapi.endpoint.rest.model.EventParticipant toUpdate1 = toUpdate.get(1);
        toUpdate1.setId("A new id one");
        List<school.hei.haapi.endpoint.rest.model.EventParticipant> updated = api.updateEventParticipants("event1_id",toUpdate);

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
