package school.hei.haapi.integration;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
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

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }

  @MockBean private CognitoComponent cognitoComponentMock;

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, FeeIT.ContextInitializer.SERVER_PORT);
  }

  static Fee fee1() {
    Fee fee = new Fee();
    fee.setId(FEE1_ID);
    fee.setStudentId(STUDENT1_ID);
    fee.setStatus(Fee.StatusEnum.UNPAID);
    fee.setType(Fee.TypeEnum.TUITION);
    fee.setTotalAmount(5000);
    fee.setRemainingAmount(5000);
    fee.setComment("Comment");
    fee.creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    fee.setDueDatetime(Instant.parse("2021-12-08T08:25:24.00Z"));
    return fee;
  }

  @Test
  void teacher_read_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    PayingApi api = new PayingApi(teacher1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentFeeById(STUDENT1_ID, FEE1_ID));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

    PayingApi api = new PayingApi(manager1Client);
    Fee actual = api.getStudentFeeById(STUDENT1_ID, FEE1_ID);

    assertEquals(fee1(), actual);
  }

  @Test
  void student_read_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    PayingApi api = new PayingApi(student1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentFeeById(STUDENT2_ID, FEE1_ID));
  }

  @Test
  void student_read_own_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    PayingApi api = new PayingApi(student1Client);
    Fee actual = api.getStudentFeeById(STUDENT1_ID, FEE1_ID);
    assertEquals(fee1(), actual);
  }
}
