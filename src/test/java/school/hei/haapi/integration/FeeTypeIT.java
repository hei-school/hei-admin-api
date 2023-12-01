package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.ConfigurationApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.FeeType;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = FeeTypeIT.ContextInitializer.class)
@AutoConfigureMockMvc
class FeeTypeIT {
  @MockBean private SentryConf sentryConf;
  @MockBean private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, FeeTypeIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    ConfigurationApi api = new ConfigurationApi(teacher1Client);

    List<FeeType> actual = api.getAllFeeType();

    assertEquals(1, actual.size());
    assertTrue(actual.contains(feeType1()));
    assertEquals(2, Objects.requireNonNull(actual.get(0).getTypes()).size());
    assertTrue(Objects.requireNonNull(actual.get(0).getTypes()).contains(feeTypeController1()));
    assertTrue(Objects.requireNonNull(actual.get(0).getTypes()).contains(feeTypeController2()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    ConfigurationApi api = new ConfigurationApi(manager1Client);

    List<FeeType> actual = api.getAllFeeType();

    assertEquals(1, actual.size());
    assertTrue(actual.contains(feeType1()));
    assertEquals(2, Objects.requireNonNull(actual.get(0).getTypes()).size());
    assertTrue(Objects.requireNonNull(actual.get(0).getTypes()).contains(feeTypeController1()));
    assertTrue(Objects.requireNonNull(actual.get(0).getTypes()).contains(feeTypeController2()));
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    ConfigurationApi api = new ConfigurationApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}", () -> api.getAllFeeType());
  }

  @Test
  @DirtiesContext
  void manager_write_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    ConfigurationApi api = new ConfigurationApi(manager1Client);

    FeeType createFeeType =
        newFeeType1(List.of(newFeeTypeController("new"), newFeeTypeController("new2")));

    FeeType newFeeType = api.createOrUpdateFeeType(createFeeType.getId(), createFeeType);
    FeeType updateFeeType =
        api.createOrUpdateFeeType(
            FEE_TYPE1_ID, feeType1().types(List.of(newFeeTypeController("new"))));

    assertEquals(2, Objects.requireNonNull(newFeeType.getTypes()).size());

    assertEquals(1, Objects.requireNonNull(updateFeeType.getTypes()).size());
    assertTrue("new".equals(Objects.requireNonNull(updateFeeType.getTypes()).get(0).getName()));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    ConfigurationApi api = new ConfigurationApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            api.createOrUpdateFeeType(
                FEE_TYPE1_ID, feeType1().types(List.of(newFeeTypeController("new")))));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    ConfigurationApi api = new ConfigurationApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            api.createOrUpdateFeeType(
                FEE_TYPE1_ID, feeType1().types(List.of(newFeeTypeController("new")))));
  }

  @Test
  void manager_write_with_some_bad_fields_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    ConfigurationApi api = new ConfigurationApi(manager1Client);

    String wrongUID = "notUID";

    FeeType createFeeType1 =
        newFeeType1(List.of(newFeeTypeController("newTypeController").name(null)));
    FeeType createFeeType2 =
        newFeeType1(List.of(newFeeTypeController("newTypeController").type(null)));
    FeeType createFeeType3 =
        newFeeType1(List.of(newFeeTypeController("newTypeController").monthlyAmount(null)));
    FeeType createFeeType4 =
        newFeeType1(List.of(newFeeTypeController("newTypeController").monthsNumber(null)));
    FeeType createFeeType5 = newFeeType1(new ArrayList<>());
    FeeType createFeeType6 =
        newFeeType1(List.of(newFeeTypeController("newTypeController"))).name(null);
    FeeType createFeeType7 =
        newFeeType1(List.of(newFeeTypeController("newTypeController"))).id(wrongUID);

    ApiException exception1 =
        assertThrows(
            ApiException.class,
            () -> api.createOrUpdateFeeType(createFeeType1.getId(), createFeeType1));

    ApiException exception2 =
        assertThrows(
            ApiException.class,
            () -> api.createOrUpdateFeeType(createFeeType2.getId(), createFeeType2));

    ApiException exception3 =
        assertThrows(
            ApiException.class,
            () -> api.createOrUpdateFeeType(createFeeType3.getId(), createFeeType3));

    ApiException exception4 =
        assertThrows(
            ApiException.class,
            () -> api.createOrUpdateFeeType(createFeeType4.getId(), createFeeType4));

    ApiException exception5 =
        assertThrows(
            ApiException.class,
            () -> api.createOrUpdateFeeType(createFeeType5.getId(), createFeeType5));

    ApiException exception6 =
        assertThrows(
            ApiException.class,
            () -> api.createOrUpdateFeeType(createFeeType6.getId(), createFeeType6));

    ApiException exception7 =
        assertThrows(
            ApiException.class,
            () -> api.createOrUpdateFeeType(createFeeType7.getId(), createFeeType7));

    String exceptionMessage1 = exception1.getMessage();
    String exceptionMessage2 = exception2.getMessage();
    String exceptionMessage3 = exception3.getMessage();
    String exceptionMessage4 = exception4.getMessage();
    String exceptionMessage5 = exception5.getMessage();
    String exceptionMessage6 = exception6.getMessage();
    String exceptionMessage7 = exception7.getMessage();

    assertTrue(exceptionMessage1.contains("feeTypeComponent have to have a name"));
    assertTrue(exceptionMessage2.contains("feeTypeComponent have to have a Type"));
    assertTrue(exceptionMessage3.contains("feeTypeComponent have to have a MonthlyAmount"));
    assertTrue(exceptionMessage4.contains("feeTypeComponent have to have a MonthsNumber"));
    assertTrue(exceptionMessage5.contains("Type is mandatory"));
    assertTrue(exceptionMessage6.contains("Name is mandatory"));
    assertTrue(exceptionMessage7.contains("The Id " + wrongUID + " must be an UID."));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
