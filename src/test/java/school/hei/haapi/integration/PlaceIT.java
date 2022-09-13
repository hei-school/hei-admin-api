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
import school.hei.haapi.endpoint.rest.model.Place;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.PLACE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.PLACE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.PLACE3_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
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

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, PlaceIT.ContextInitializer.SERVER_PORT);
    }

    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    static Place place1() {
        return new Place()
                .id(PLACE1_ID)
                .name("place1")
                .roomRef("ref1");
    }

    static Place place2() {
        return new Place()
                .id(PLACE2_ID)
                .name("place2")
                .roomRef("ref2");
    }

    static Place place3() {
        return new Place()
                .id(PLACE3_ID)
                .name("place3");
    }

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        PlaceApi api = new PlaceApi(student1Client);
        List<Place> places = api.getPlaces();

        Place actual = api.getPlaceById(PLACE1_ID);
        assertEquals(place1(), actual);
        assertTrue(places.contains(actual));
    }

    @Test
    void manager_read_ok() throws ApiException {
        ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
        PlaceApi api = new PlaceApi(managerClient);
        List<Place> places = api.getPlaces();

        Place actual = api.getPlaceById(PLACE1_ID);
        assertEquals(place1(), actual);
        assertTrue(places.contains(actual));
    }

    @Test
    void teacher_read_ok() throws ApiException {
        ApiClient teacherClient = anApiClient(MANAGER1_TOKEN);
        PlaceApi api = new PlaceApi(teacherClient);
        List<Place> places = api.getPlaces();

        Place actual = api.getPlaceById(PLACE1_ID);
        assertEquals(place1(), actual);
        assertTrue(places.contains(actual));
    }

    @Test
    void manager_write_ok() throws ApiException {
        ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
        PlaceApi api = new PlaceApi(managerClient);

        List<Place> actual = api.createPlaces(List.of(place1(), place2(), place3()));
        List<Place> expected = api.getPlaces();

        assertTrue(expected.containsAll(actual));
    }

    @Test
    void student_write_ko() {
        ApiClient studentClient = anApiClient(STUDENT1_TOKEN);
        PlaceApi api = new PlaceApi(studentClient);

        assertThrowsForbiddenException(()->api.createPlaces(List.of()));
    }

    @Test
    void teacher_write_ko() {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        PlaceApi api = new PlaceApi(teacher1Client);

        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.createPlaces(List.of()));
    }
}
