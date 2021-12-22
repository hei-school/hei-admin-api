package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.WhoAmIApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Whoami;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = WhoamiIT.ContextInitializer.class)
@AutoConfigureMockMvc
class WhoamiIT {

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = 8084;

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }

  private static ApiClient aClient(String token) {
    return TestUtils.aClient(token, ContextInitializer.SERVER_PORT);
  }

  @MockBean private CognitoComponent cognitoComponent;

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponent);
  }

  @Test
  void student_read_own_ok() throws ApiException {
    ApiClient student1Client = aClient(TestUtils.STUDENT1_TOKEN);

    WhoAmIApi api = new WhoAmIApi(student1Client);
    Whoami actual = api.whoami();

    assertEquals(whoisStudent1(), actual);
  }

  @Test
  void teacher_read_own_ok() throws ApiException {
    ApiClient teacher1Client = aClient(TestUtils.TEACHER1_TOKEN);

    WhoAmIApi api = new WhoAmIApi(teacher1Client);
    Whoami actual = api.whoami();

    assertEquals(whoisTeacher1(), actual);
  }

  @Test
  void manager_read_own_ok() throws ApiException {
    ApiClient manager1Client = aClient(TestUtils.MANAGER1_TOKEN);

    WhoAmIApi api = new WhoAmIApi(manager1Client);
    Whoami actual = api.whoami();

    assertEquals(whoisManager1(), actual);
  }

  public static Whoami whoisStudent1() {
    Whoami whoami = new Whoami();
    whoami.setId("student1_id");
    whoami.setRole(Whoami.RoleEnum.STUDENT);
    return whoami;
  }

  public static Whoami whoisTeacher1() {
    Whoami whoami = new Whoami();
    whoami.setId("teacher1_id");
    whoami.setRole(Whoami.RoleEnum.TEACHER);
    return whoami;
  }

  public static Whoami whoisManager1() {
    Whoami whoami = new Whoami();
    whoami.setId("manager1_id");
    whoami.setRole(Whoami.RoleEnum.MANAGER);
    return whoami;
  }
}
