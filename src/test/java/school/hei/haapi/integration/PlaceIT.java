package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import java.util.List;
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
import school.hei.haapi.endpoint.rest.model.Place;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

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
    return new Place()
        .id("place1_id")
        .location("Haute Ecole d'Informatique")
        .city("Antananarivo");
  }

  public static Place place2() {
    return new Place()
        .id("place2_id")
        .city("Antananarivo")
        .location("Alliance Francaise");
  }

  public static Place aCreatablePlace() {
    return new Place()
        .id("place 21-" + randomUUID())
        .city("some city")
        .location("some location")
        ;
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void badtoken_read_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    PlacesApi api = new PlacesApi(anonymousClient);
    assertThrowsForbiddenException(api::getPlaces);
  }

  @Test
  void badtoken_write_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    PlacesApi api = new PlacesApi(anonymousClient);
    assertThrowsForbiddenException(() -> api.createOrUpdatePlace(new Place()));
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PlacesApi api = new PlacesApi(student1Client);

    List<Place> actualPlaces = api.getPlaces();

    assertTrue(actualPlaces.contains(place1()));
    assertTrue(actualPlaces.contains(place2()));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdatePlace(place1()));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    PlacesApi api = new PlacesApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdatePlace(place1()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(MANAGER1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);

    List<Place> actualPlaces = api.getPlaces();

    assertTrue(actualPlaces.contains(place1()));
    assertTrue(actualPlaces.contains(place2()));
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    Place toCreate1 = aCreatablePlace();

    PlacesApi api = new PlacesApi(manager1Client);
    Place created = api.createOrUpdatePlace(toCreate1);
    toCreate1.setId(created.getId());

    assertEquals(toCreate1, created);
    assertTrue(isValidUUID(created.getId()));
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PlacesApi api = new PlacesApi(manager1Client);

    Place toUpdate = api.createOrUpdatePlace(
        aCreatablePlace());
    toUpdate.setLocation("A new location zero");
    Place updated = api.createOrUpdatePlace(toUpdate);
    toUpdate.setId(updated.getId());

    assertEquals(toUpdate, updated);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
