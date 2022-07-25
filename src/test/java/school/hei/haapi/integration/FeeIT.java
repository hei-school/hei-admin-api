package school.hei.haapi.integration;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.FEE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE3_ID;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = FeeIT.ContextInitializer.class)
@AutoConfigureMockMvc
class FeeIT {
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

  static Fee fee1() {
    Fee fee = new Fee();
    fee.setId(FEE1_ID);
    fee.setStudentId(STUDENT1_ID);
    fee.setStatus(Fee.StatusEnum.PAID);
    fee.setType(Fee.TypeEnum.TUITION);
    fee.setTotalAmount(5000);
    fee.setRemainingAmount(0);
    fee.setComment("Comment");
    fee.creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    fee.setDueDatetime(Instant.parse("2021-12-08T08:25:24.00Z"));
    return fee;
  }

  static Fee fee2() {
    Fee fee = new Fee();
    fee.setId(FEE2_ID);
    fee.setStudentId(STUDENT1_ID);
    fee.setStatus(Fee.StatusEnum.PAID);
    fee.setType(Fee.TypeEnum.HARDWARE);
    fee.setTotalAmount(5000);
    fee.setRemainingAmount(0);
    fee.setComment("Comment");
    fee.creationDatetime(Instant.parse("2021-11-10T08:25:24.00Z"));
    fee.setDueDatetime(Instant.parse("2021-12-10T08:25:24.00Z"));
    return fee;
  }

  static Fee fee3() {
    Fee fee = new Fee();
    fee.setId(FEE3_ID);
    fee.setStudentId(STUDENT1_ID);
    fee.setStatus(Fee.StatusEnum.LATE);
    fee.setType(Fee.TypeEnum.TUITION);
    fee.setTotalAmount(5000);
    fee.setRemainingAmount(5000);
    fee.setComment("Comment");
    fee.creationDatetime(Instant.parse("2022-12-08T08:25:24.00Z"));
    fee.setDueDatetime(Instant.parse("2021-12-09T08:25:24.00Z"));
    return fee;
  }

  static CreateFee creatableFee1() {
    return new CreateFee()
        .type(CreateFee.TypeEnum.TUITION)
        .totalAmount(5000)
        .comment("Comment")
        .dueDatetime(Instant.parse("2021-12-08T08:25:24.00Z"));
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
    List<Fee> actualFees2 = api.getFees(String.valueOf(Fee.StatusEnum.PAID), 1, 10);

    assertEquals(fee1(), actualFee);
    assertEquals(4, actualFees2.size());
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

    List<Fee> actual = api.createStudentFees(STUDENT1_ID, List.of(creatableFee1()));

    List<Fee> expected = api.getStudentFees(STUDENT1_ID, 1, 5, null);
    assertTrue(expected.containsAll(actual));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createStudentFees(STUDENT1_ID, List.of()));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createStudentFees(STUDENT1_ID, List.of()));
  }

  @Test
  void manager_write_with_some_bad_fields_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    CreateFee toCreate1 = creatableFee1().totalAmount(null);
    CreateFee toCreate2 = creatableFee1().totalAmount(-1);
    CreateFee toCreate3 = creatableFee1().dueDatetime(null);
    List<Fee> expected = api.getStudentFees(STUDENT1_ID, 1, 5, null);

    ApiException exception1 = assertThrows(ApiException.class,
        () -> api.createStudentFees(STUDENT1_ID, List.of(toCreate1)));
    ApiException exception2 = assertThrows(ApiException.class,
        () -> api.createStudentFees(STUDENT1_ID, List.of(toCreate2)));
    ApiException exception3 = assertThrows(ApiException.class,
        () -> api.createStudentFees(STUDENT1_ID, List.of(toCreate3)));

    List<Fee> actual = api.getStudentFees(STUDENT1_ID, 1, 5, null);
    assertEquals(expected.size(), actual.size());
    assertTrue(expected.containsAll(actual));
    String exceptionMessage1 = exception1.getMessage();
    String exceptionMessage2 = exception2.getMessage();
    String exceptionMessage3 = exception3.getMessage();
    assertTrue(exceptionMessage1.contains("Total amount is mandatory"));
    assertTrue(exceptionMessage2.contains("Total amount must be positive"));
    assertTrue(exceptionMessage3.contains("Due datetime is mandatory"));
  }
}
