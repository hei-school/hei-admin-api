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
import school.hei.haapi.endpoint.rest.api.PlacesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Place;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
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

 public static Place place1(){
    Place place = new Place();
    place.setId("place1_id");
    place.setName("Alliance Française");
    place.setLocation("Andavamamba");
    place.setRegion("Antananarivo");
    return place;
 }

  public static Place place2(){
    Place place = new Place();
    place.setId("place2_id");
    place.setName("HEI");
    place.setLocation("Ivandry");
    place.setRegion("Antananarivo");
    return place;
  }

  public static Place someCreatablePlace1() {
    Place place = new Place();
    place.setId("place3_id");
    place.setName("Random place");
    place.setLocation("Ivandry");
    place.setRegion("Antananarivo");
    return place;
  }
  public static Place someCreatablePlace2() {
    Place place = new Place();
    place.setId("place4_id");
    place.setName("Room");
    place.setLocation("Ivandry");
    place.setRegion("Antananarivo");
    return place;
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void student_read_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    assertThrowsForbiddenException(() -> api.getPlaces(1,20));

  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdatePlaces(List.of(someCreatablePlace1(), someCreatablePlace2())));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(TEACHER1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    List<Place> places = api.getPlaces(1,20);

    assertTrue(places.contains(place1()));
    assertTrue(places.contains(place2()));
  }
  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    PlacesApi api = new PlacesApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.createOrUpdatePlaces(List.of(someCreatablePlace1(), someCreatablePlace2())));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(MANAGER1_TOKEN);

    PlacesApi api = new PlacesApi(student1Client);
    List<Place> places = api.getPlaces(1,20);

    assertTrue(places.contains(place1()));
    assertTrue(places.contains(place2()));
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    Place toCreate1 = someCreatablePlace1();
    Place toCreate2 = someCreatablePlace2();


    PlacesApi api = new PlacesApi(manager1Client);
    List<Place> created = api.createOrUpdatePlaces(List.of(toCreate1, toCreate2));

    assertEquals(2, created.size());
    Place created1 = created.get(0);
    assertTrue(isValidUUID(created1.getId()));
    toCreate1.setId(created1.getId());
    //
    assertEquals(created1, toCreate1);
    Place created2 = created.get(0);
    assertTrue(isValidUUID(created2.getId()));
    toCreate2.setId(created2.getId());
    assertEquals(created2, toCreate1);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
