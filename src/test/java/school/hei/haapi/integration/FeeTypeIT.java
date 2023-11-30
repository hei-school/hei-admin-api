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

    FeeType newFeeType =
        api.createOrUpdateFeeType(
            newFeeType1(List.of(newFeeTypeController("new"), newFeeTypeController("new2"))));
    FeeType updateFeeType =
        api.createOrUpdateFeeType(feeType1().types(List.of(newFeeTypeController("new"))));

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
        () -> api.createOrUpdateFeeType(feeType1().types(List.of(newFeeTypeController("new")))));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    ConfigurationApi api = new ConfigurationApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateFeeType(feeType1().types(List.of(newFeeTypeController("new")))));
  }

  @Test
  void manager_write_with_some_bad_fields_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    ConfigurationApi api = new ConfigurationApi(manager1Client);

    ApiException exception1 =
        assertThrows(
            ApiException.class,
            () ->
                api.createOrUpdateFeeType(
                    newFeeType1(List.of(newFeeTypeController("newTypeController").name(null)))));

    ApiException exception2 =
        assertThrows(
            ApiException.class,
            () ->
                api.createOrUpdateFeeType(
                    newFeeType1(List.of(newFeeTypeController("newTypeController").type(null)))));

    ApiException exception3 =
        assertThrows(
            ApiException.class,
            () ->
                api.createOrUpdateFeeType(
                    newFeeType1(
                        List.of(newFeeTypeController("newTypeController").monthlyAmount(null)))));

    ApiException exception4 =
        assertThrows(
            ApiException.class,
            () ->
                api.createOrUpdateFeeType(
                    newFeeType1(
                        List.of(newFeeTypeController("newTypeController").monthsNumber(null)))));

    ApiException exception5 =
        assertThrows(
            ApiException.class, () -> api.createOrUpdateFeeType(newFeeType1(new ArrayList<>())));

    ApiException exception6 =
        assertThrows(
            ApiException.class,
            () ->
                api.createOrUpdateFeeType(
                    newFeeType1(List.of(newFeeTypeController("newTypeController"))).name(null)));

    String exceptionMessage1 = exception1.getMessage();
    String exceptionMessage2 = exception2.getMessage();
    String exceptionMessage3 = exception3.getMessage();
    String exceptionMessage4 = exception4.getMessage();
    String exceptionMessage5 = exception5.getMessage();
    String exceptionMessage6 = exception6.getMessage();

    assertTrue(exceptionMessage1.contains("feeTypeComponent have to have a name"));
    assertTrue(exceptionMessage2.contains("feeTypeComponent have to have a Type"));
    assertTrue(exceptionMessage3.contains("feeTypeComponent have to have a MonthlyAmount"));
    assertTrue(exceptionMessage4.contains("feeTypeComponent have to have a MonthsNumber"));
    assertTrue(exceptionMessage5.contains("Type is mandatory"));
    assertTrue(exceptionMessage6.contains("Name is mandatory"));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
