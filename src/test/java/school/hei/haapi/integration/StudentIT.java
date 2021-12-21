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

import java.sql.Date;
import java.time.Instant;

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
    Student student1 = api.getStudentById(TestUtils.STUDENT1_ID);

    Student expectedStudent1 = new Student();
    expectedStudent1.setId("student1_id");
    expectedStudent1.setFirstName("Ryan");
    expectedStudent1.setLastName("Andria");
    expectedStudent1.setEmail("ryan@hei.school");
    expectedStudent1.setRef("STD21001");
    expectedStudent1.setPhone("0322411123");
    expectedStudent1.setStatus(Student.StatusEnum.ENABLED);
    expectedStudent1.setSex(Student.SexEnum.M);
    expectedStudent1.setBirthDate(Date.valueOf("2000-01-01"));
    expectedStudent1.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    expectedStudent1.setAddress("Adr 1");
    expectedStudent1.setGroupId("group1_id");
    assertEquals(expectedStudent1, student1);
  }

  @Test
  void student1_can_not_get_student2_information() {
    ApiClient student1Client = aClient(TestUtils.STUDENT1_TOKEN);

    StudentsApi api = new StudentsApi(student1Client);
    assertThrowsApiException(
        () -> api.getStudentById(TestUtils.STUDENT2_ID),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Students can only read their own information\"}");
  }

  @Test
  void teacher1_can_get_student1_information() throws ApiException {
    ApiClient teacher1Client = aClient(TestUtils.TEACHER1_TOKEN);

    StudentsApi api = new StudentsApi(teacher1Client);
    Student student = api.getStudentById(TestUtils.STUDENT1_ID);

    assertEquals("Ryan", student.getFirstName());
  }
}
