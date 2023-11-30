package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.creatableFee1;
import static school.hei.haapi.integration.conf.TestUtils.fee1;
import static school.hei.haapi.integration.conf.TestUtils.fee2;
import static school.hei.haapi.integration.conf.TestUtils.fee3;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = FeeIT.ContextInitializer.class)
@AutoConfigureMockMvc
class FeeIT {
  @MockBean private SentryConf sentryConf;
  @MockBean private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, FeeIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    Fee actualFee = api.getStudentFeeById(STUDENT1_ID, FEE1_ID);
    List<Fee> actual = api.getStudentFees(STUDENT1_ID, 1, 5, null);

    assertEquals(fee1(), actualFee);
    assertTrue(actual.contains(fee1()));
    assertTrue(actual.contains(fee2()));
    assertTrue(actual.contains(fee3()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    Fee actualFee = api.getStudentFeeById(STUDENT1_ID, FEE1_ID);
    List<Fee> actualFees1 = api.getStudentFees(STUDENT1_ID, 1, 5, null);
    List<Fee> actualFees2 = api.getFees(PAID.toString(), 1, 10);

    assertEquals(fee1(), actualFee);
    assertEquals(2, actualFees2.size());
    assertTrue(actualFees1.contains(fee1()));
    assertTrue(actualFees1.contains(fee2()));
    assertTrue(actualFees1.contains(fee3()));
    assertTrue(actualFees2.contains(fee1()));
    assertTrue(actualFees2.contains(fee2()));
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentFeeById(STUDENT2_ID, FEE2_ID));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentFees(STUDENT2_ID, null, null, null));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getFees(null, null, null));
  }

  @Test
  void teacher_read_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentFeeById(STUDENT2_ID, FEE2_ID));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentFees(STUDENT2_ID, null, null, null));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getFees(null, null, null));
  }

  @Test
  void manager_write_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    Fee updatedFee =
        fee1().comment("M1 + M2 + M3").dueDatetime(Instant.parse("2021-11-09T10:10:10.00Z"));

    List<Fee> actual = api.createStudentFees(STUDENT1_ID, List.of(creatableFee1()));

    List<Fee> actualUpdated = api.updateStudentFees(STUDENT1_ID, List.of(updatedFee));

    List<Fee> expected = api.getStudentFees(STUDENT1_ID, 1, 5, null);
    assertTrue(expected.containsAll(actual));

    assertEquals(1, actualUpdated.size());
    assertEquals(actualUpdated.get(0).getComment(), updatedFee.getComment());
    assertEquals(actualUpdated.get(0).getDueDatetime(), updatedFee.getDueDatetime());
    assertTrue(expected.containsAll(actual));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);
    Fee feeUpdated =
        fee1().comment("nex comment").dueDatetime(Instant.parse("2021-11-09T10:10:10.00Z"));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.updateStudentFees(STUDENT1_ID, List.of(feeUpdated)));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createStudentFees(STUDENT1_ID, List.of()));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);
    Fee feeUpdated =
        fee1().comment("nex comment").dueDatetime(Instant.parse("2021-11-09T10:10:10.00Z"));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.updateStudentFees(STUDENT1_ID, List.of(feeUpdated)));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createStudentFees(STUDENT1_ID, List.of()));
  }

  @DirtiesContext
  @Test
  void manager_write_with_some_bad_fields_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    CreateFee toCreate1 = creatableFee1().totalAmount(null);
    CreateFee toCreate2 = creatableFee1().totalAmount(-1);
    CreateFee toCreate3 = creatableFee1().dueDatetime(null);
    String wrongId = "wrong id";
    List<Fee> expected = api.getStudentFees(STUDENT1_ID, 1, 5, null);

    ApiException exception1 =
        assertThrows(
            ApiException.class, () -> api.createStudentFees(STUDENT1_ID, List.of(toCreate1)));
    ApiException exception2 =
        assertThrows(
            ApiException.class, () -> api.createStudentFees(STUDENT1_ID, List.of(toCreate2)));
    ApiException exception3 =
        assertThrows(
            ApiException.class, () -> api.createStudentFees(STUDENT1_ID, List.of(toCreate3)));
    ApiException exception4 =
        assertThrows(
            ApiException.class, () -> api.updateStudentFees(STUDENT1_ID, List.of(fee1().id(null))));
    ApiException exception6 =
        assertThrows(
            ApiException.class,
            () -> api.updateStudentFees(STUDENT1_ID, List.of(fee1().type(Fee.TypeEnum.HARDWARE))));
    ApiException exception7 =
        assertThrows(
            ApiException.class,
            () -> api.updateStudentFees(STUDENT1_ID, List.of(fee1().remainingAmount(10))));
    ApiException exception9 =
        assertThrows(
            ApiException.class,
            () -> api.updateStudentFees(STUDENT1_ID, List.of(fee1().totalAmount(10))));
    ApiException exception10 =
        assertThrows(
            ApiException.class,
            () ->
                api.updateStudentFees(
                    STUDENT1_ID,
                    List.of(fee1().creationDatetime(Instant.parse("2021-11-09T10:10:10.00Z")))));
    ApiException exception11 =
        assertThrows(
            ApiException.class,
            () -> api.updateStudentFees(STUDENT1_ID, List.of(fee1().id(wrongId))));

    String exceptionMessage1 = exception1.getMessage();
    String exceptionMessage2 = exception2.getMessage();
    String exceptionMessage3 = exception3.getMessage();
    String exceptionMessage4 = exception4.getMessage();
    String exceptionMessage6 = exception6.getMessage();
    String exceptionMessage7 = exception7.getMessage();
    String exceptionMessage9 = exception9.getMessage();
    String exceptionMessage10 = exception10.getMessage();
    String exceptionMessage11 = exception11.getMessage();

    List<Fee> actual = api.getStudentFees(STUDENT1_ID, 1, 5, null);
    assertEquals(expected.size(), actual.size());

    assertTrue(expected.containsAll(actual));
    assertTrue(exceptionMessage1.contains("Total amount is mandatory"));
    assertTrue(exceptionMessage2.contains("Total amount must be positive"));
    assertTrue(exceptionMessage3.contains("Due datetime is mandatory"));

    assertTrue(exceptionMessage4.contains("Id is mandatory"));
    assertTrue(exceptionMessage6.contains("Can't modify Type"));
    assertTrue(exceptionMessage7.contains("Can't modify remainingAmount"));
    assertTrue(exceptionMessage9.contains("Can't modify total amount"));
    assertTrue(exceptionMessage10.contains("Can't modify CreationDatetime"));
    assertTrue(exceptionMessage11.contains("Fee with id " + wrongId + "does not exist"));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
