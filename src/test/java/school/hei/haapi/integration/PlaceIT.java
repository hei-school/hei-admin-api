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
import school.hei.haapi.endpoint.rest.model.Place;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.PLACE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PlaceIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class PlaceIT {

    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
    }

    public static Place place1() {
        Place place = new Place();
        place.setId("place1_id");
        place.setName("Name of place one");
        return place;
    }

    public static Place place2() {
        Place place = new Place();
        place.setId("place2_id");
        place.setName("Name of place two");
        return place;
    }

    public static Place someCreatablePlace() {
        Place place = new Place();
        place.setName("Some name");
        return place;
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
    void badtoken_write_ko() {
        ApiClient anonymousClient = anApiClient(BAD_TOKEN);

        EventApi api = new EventApi(anonymousClient);
        assertThrowsForbiddenException(() -> api.createOrUpdatePlaces(List.of()));
    }

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

        EventApi api = new EventApi(student1Client);
        Place actual1 = api.getPlaceById(PLACE1_ID);
        List<Place> actualPlaces = api.getPlaces();

        assertEquals(place1(), actual1);
        assertTrue(actualPlaces.contains(place1()));
        assertTrue(actualPlaces.contains(place2()));
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
        assertThrowsForbiddenException(() -> api.createOrUpdatePlaces(List.of()));
    }

    @Test
    void manager_write_create_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        Place toCreate3 = someCreatablePlace();
        Place toCreate4 = someCreatablePlace();

        EventApi api = new EventApi(manager1Client);
        List<Place> created = api.createOrUpdatePlaces(List.of(toCreate3, toCreate4));

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
        List<Place> toUpdate = api.createOrUpdatePlaces(List.of(
                someCreatablePlace(),
                someCreatablePlace()));
        Place toUpdate0 = toUpdate.get(0);
        toUpdate0.setName("A new name zero");
        Place toUpdate1 = toUpdate.get(1);
        toUpdate1.setName("A new name one");

        List<Place> updated = api.createOrUpdatePlaces(toUpdate);

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
