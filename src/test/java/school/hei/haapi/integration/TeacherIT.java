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

import java.sql.Date;
import java.time.Instant;

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
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Students cannot read teachers\"}");
  }

  @Test
  void teacher1_can_not_get_teacher2_information() {
    ApiClient teacher1Client = aClient(TestUtils.TEACHER1_TOKEN);

    TeachersApi api = new TeachersApi(teacher1Client);
    assertThrowsApiException(
        () -> api.getTeacherById(TestUtils.TEACHER2_ID),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Teachers can only read their own information\"}");  }

  @Test
  void teacher1_can_get_his_information() throws ApiException {
    ApiClient teacher1Client = aClient(TestUtils.TEACHER1_TOKEN);

    TeachersApi api = new TeachersApi(teacher1Client);
    Teacher teacher1 = api.getTeacherById(TestUtils.TEACHER1_ID);

    Teacher expectedTeacher1 = new Teacher();
    expectedTeacher1.setId("teacher1_id");
    expectedTeacher1.setFirstName("One");
    expectedTeacher1.setLastName("Teacher");
    expectedTeacher1.setEmail("teacher1@hei.school");
    expectedTeacher1.setRef("TCR21001");
    expectedTeacher1.setPhone("0322411125");
    expectedTeacher1.setStatus(Teacher.StatusEnum.ENABLED);
    expectedTeacher1.setSex(Teacher.SexEnum.F);
    expectedTeacher1.setBirthDate(Date.valueOf("1990-01-01"));
    expectedTeacher1.setEntranceDatetime(Instant.parse("2021-10-08T08:27:24.00Z"));
    expectedTeacher1.setAddress("Adr 3");
    assertEquals(expectedTeacher1, teacher1);
  }
}
