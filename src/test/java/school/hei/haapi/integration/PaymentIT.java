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
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.endpoint.rest.model.Payment;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE3_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PaymentIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class PaymentIT {
  @MockBean private SentryConf sentryConf;
  @MockBean private CognitoComponent cognitoComponentMock;

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, PaymentIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  static Payment payment1() {
    return new Payment()
        .id("payment1_id")
        .feeId(FEE1_ID)
        .type(Payment.TypeEnum.CASH)
        .amount(2000)
        .comment("Comment")
        .creationDatetime(Instant.parse("2022-11-08T08:25:24.00Z"));
  }

  static CreatePayment creatablePayment1() {
    return new CreatePayment()
        .feeId("fee1_id")
        .type(CreatePayment.TypeEnum.CASH)
        .amount(2000)
        .comment("Comment");
  }

  static CreatePayment creatablePayment3() {
    return new CreatePayment()
        .feeId("fee3_id")
        .type(CreatePayment.TypeEnum.CASH)
        .amount(6000)
        .comment("Comment");
  }

  @Test
  void student_read_own_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    List<Payment> actual = api.getStudentFeePayments(STUDENT1_ID, FEE1_ID);

    assertTrue(actual.contains(payment1()));
  }

  @Test
  void student_read_all_own_ok() throws ApiException {
    ApiClient student2Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student2Client);

    List<Payment> actual = api.getStudentPayments(STUDENT1_ID);

    assertTrue(actual.contains(payment1()));
  }

  @Test
  void student1_read_all_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentPayments(STUDENT2_ID));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    List<Payment> student1Payments = api.getStudentFeePayments(STUDENT1_ID, FEE1_ID);

    assertTrue(student1Payments.contains(payment1()));
  }

  @Test
  void teacher_read_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentFeePayments(STUDENT2_ID, FEE1_ID));
  }

  @Test
  void manager_write_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    CreatePayment toCreate = creatablePayment3().amount(5000);

    List<Payment> actualPayments = api.createStudentPayments(FEE3_ID,
        List.of(toCreate));

    List<Payment> expectedPayments = api.getStudentPayments(STUDENT2_ID);
    assertTrue(expectedPayments.containsAll(actualPayments));
  }

  @Test
  void manager_write_ko() {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Payment must be associated to fee.fee1_id"
            + " instead of fee.fee3_id\"}",
        () -> api.createStudentPayments(FEE3_ID, List.of(creatablePayment1())));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Fee remaining amount is 5000"
            + ". Actual payment amount is 6000\"}",
        () -> api.createStudentPayments(FEE3_ID, List.of(creatablePayment3())));
  }

  @Test
  void manager_write_with_some_bad_fields_ko() {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    CreatePayment toCreate1 = creatablePayment1().amount(null);
    CreatePayment toCreate2 = creatablePayment1().amount(-1);

    ApiException exception1 = assertThrows(ApiException.class,
        () -> api.createStudentPayments(FEE1_ID, List.of(toCreate1)));
    ApiException exception2 = assertThrows(ApiException.class,
        () -> api.createStudentPayments(FEE1_ID, List.of(toCreate2)));

    String exceptionMessage1 = exception1.getMessage();
    String exceptionMessage2 = exception2.getMessage();
    assertTrue(exceptionMessage1.contains("Amount is mandatory"));
    assertTrue(exceptionMessage2.contains("Amount must be positive"));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createStudentPayments(FEE1_ID, List.of()));

  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createStudentPayments(FEE1_ID, List.of()));
  }
}
