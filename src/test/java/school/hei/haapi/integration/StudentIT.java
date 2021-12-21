package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.StudentsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.endpoint.rest.model.Student;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = StudentIT.ContextInitializer.class)
@AutoConfigureMockMvc
class StudentIT {

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = 8082;

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
  void student1_can_get_his_information() throws ApiException {
    ApiClient student1Client = aClient(TestUtils.STUDENT1_TOKEN);

    StudentsApi api = new StudentsApi(student1Client);
    Student student = api.findStudentById(TestUtils.STUDENT1_ID);

    assertEquals("Ryan", student.getFirstName());
  }

  @Test
  void student1_can_not_get_student2_information() {
    ApiClient student1Client = aClient(TestUtils.STUDENT1_TOKEN);

    StudentsApi api = new StudentsApi(student1Client);
    assertThrowsApiException(
        () -> api.findStudentById(TestUtils.STUDENT2_ID),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Students can only get their own information\"}");
  }

  @Test
  void teacher1_can_get_student1_information() throws ApiException {
    ApiClient teacher1Client = aClient(TestUtils.TEACHER1_TOKEN);

    StudentsApi api = new StudentsApi(teacher1Client);
    Student student = api.findStudentById(TestUtils.STUDENT1_ID);

    assertEquals("Ryan", student.getFirstName());
  }
}
