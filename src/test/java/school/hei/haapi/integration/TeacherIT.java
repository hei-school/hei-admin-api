package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.TeachersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = TeacherIT.ContextInitializer.class)
@AutoConfigureMockMvc
class TeacherIT {

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = 8083;

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
  void student1_can_not_get_teacher1_information() {
    ApiClient student1Client = aClient(TestUtils.STUDENT1_TOKEN);

    TeachersApi api = new TeachersApi(student1Client);
    assertThrowsApiException(
        () -> api.getTeacherById(TestUtils.TEACHER1_ID),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Students are not allowed to access teacher information\"}");
  }

  @Test
  void teacher1_can_not_get_teacher2_information() {
    ApiClient teacher1Client = aClient(TestUtils.TEACHER1_TOKEN);

    TeachersApi api = new TeachersApi(teacher1Client);
    assertThrowsApiException(
        () -> api.getTeacherById(TestUtils.TEACHER2_ID),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"A teacher cannot access information of another teacher\"}");  }

  @Test
  void teacher1_can_get_his_information() throws ApiException {
    ApiClient teacher1Client = aClient(TestUtils.TEACHER1_TOKEN);

    TeachersApi api = new TeachersApi(teacher1Client);
    Teacher teacher = api.getTeacherById(TestUtils.TEACHER1_ID);

    assertEquals("One", teacher.getFirstName());
    assertEquals("Teacher", teacher.getLastName());
  }
}
