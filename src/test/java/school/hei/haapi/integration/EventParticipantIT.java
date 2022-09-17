package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.PlaceApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateEventParticipant;
import school.hei.haapi.endpoint.rest.model.EventParticipant;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.EVENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EVENT_PARTICIPANT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.EVENT_PARTICIPANT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.EVENT_PARTICIPANT3_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class EventParticipantIT {
    @MockBean
    private SentryConf sentryConf;
    @MockBean
    private CognitoComponent cognitoComponentMock;

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, FeeIT.ContextInitializer.SERVER_PORT);
    }

    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    static EventParticipant eventParticipant1() {
        EventParticipant eventParticipant = new EventParticipant();
        eventParticipant.setId(EVENT_PARTICIPANT1_ID);
        eventParticipant.setEventId(EVENT1_ID);
        eventParticipant.setStatus(EventParticipant.StatusEnum.EXPECTED);
        eventParticipant.setUserId(STUDENT1_ID);

        return eventParticipant;
    }

    static EventParticipant eventParticipant2() {
        EventParticipant eventParticipant = new EventParticipant();
        eventParticipant.setId(EVENT_PARTICIPANT2_ID);
        eventParticipant.setEventId(EVENT1_ID);
        eventParticipant.setStatus(EventParticipant.StatusEnum.EXPECTED);
        eventParticipant.setUserId(TEACHER1_ID);

        return eventParticipant;
    }

    static EventParticipant eventParticipant3() {
        EventParticipant eventParticipant = new EventParticipant();
        eventParticipant.setId(EVENT_PARTICIPANT3_ID);
        eventParticipant.setEventId(EVENT1_ID);
        eventParticipant.setStatus(EventParticipant.StatusEnum.EXPECTED);
        eventParticipant.setUserId(MANAGER_ID);

        return eventParticipant;
    }
    static CreateEventParticipant createEventParticipant3(){
        return new CreateEventParticipant()
                .id(EVENT_PARTICIPANT3_ID)
                .userId(MANAGER_ID);
    }
    static CreateEventParticipant createEventParticipant1(){
        return new CreateEventParticipant()
                .id(EVENT_PARTICIPANT1_ID)
                .userId(STUDENT1_ID);
    }
    static CreateEventParticipant createEventParticipant2(){
        return new CreateEventParticipant()
                .id(EVENT_PARTICIPANT2_ID)
                .userId(TEACHER1_ID);
    }

    @Test
    void teacher_read_ok() throws ApiException {
        ApiClient teacher1Client =anApiClient(TEACHER1_TOKEN);
        PlaceApi api = new PlaceApi(teacher1Client);

        /* get the list of participant of event~EVENT1_ID*/
        List<EventParticipant> participants = api.getParticipants(EVENT1_ID, 1, 5);

        assertTrue(participants.contains(eventParticipant1()));
        assertTrue(participants.contains(eventParticipant2()));
        assertTrue(participants.contains(eventParticipant3()));
    }
    @Test
    void manager_read_ok() throws ApiException {
        ApiClient manager1Client =anApiClient(MANAGER1_TOKEN);
        PlaceApi api = new PlaceApi(manager1Client);

        /* get the list of participant of event~EVENT1_ID*/
        List<EventParticipant> participants = api.getParticipants(EVENT1_ID, 1, 5);

        assertTrue(participants.contains(eventParticipant1()));
        assertTrue(participants.contains(eventParticipant2()));
        assertTrue(participants.contains(eventParticipant3()));
    }
    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client =anApiClient(STUDENT1_TOKEN);
        PlaceApi api = new PlaceApi(student1Client);

        /* get the list of participant of event~EVENT1_ID*/
        List<EventParticipant> participants = api.getParticipants(EVENT1_ID, 1, 5);

        assertTrue(participants.contains(eventParticipant1()));
        assertTrue(participants.contains(eventParticipant2()));
        assertTrue(participants.contains(eventParticipant3()));
    }

    @Test
    void manager_write_ok()throws ApiException{
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        PlaceApi api = new PlaceApi(manager1Client);

        List<CreateEventParticipant> actual = List.of(createEventParticipant1(), createEventParticipant2(), createEventParticipant3());
        api.createEventParticipant(EVENT1_ID, actual);

        List<EventParticipant> participants = api.getParticipants(EVENT1_ID, 1, 5);
        assertTrue(participants.contains(eventParticipant1()));
        assertTrue(participants.contains(eventParticipant3()));
        assertTrue(participants.contains(eventParticipant2()));
    }

    @Test
    void teacher_write_ko() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        PlaceApi api = new PlaceApi(teacher1Client);

        List<CreateEventParticipant> actual = List.of(createEventParticipant1(), createEventParticipant2(), createEventParticipant3());
        assertThrowsForbiddenException(()->api.createEventParticipant(EVENT1_ID, actual));
    }
}
