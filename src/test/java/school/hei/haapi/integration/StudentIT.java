package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.endpoint.rest.model.Student;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = StudentIT.ContextInitializer.class)
@AutoConfigureMockMvc
class StudentIT {

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  @MockBean private CognitoComponent cognitoComponent;

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponent);
  }

  @Test
  void student_read_own_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    UsersApi api = new UsersApi(student1Client);
    Student actual = api.getStudentById(STUDENT1_ID);

    assertEquals(student1(), actual);
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    UsersApi api = new UsersApi(student1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Students can only read their own information\"}",
        () -> api.getStudentById(TestUtils.STUDENT2_ID)
    );
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Students can only read their own information\"}",
        api::getStudents
    );
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    UsersApi api = new UsersApi(teacher1Client);
    Student actualStudent1 = api.getStudentById(STUDENT1_ID);
    List<Student> actualStudents = api.getStudents();

    assertEquals(student1(), actualStudent1);
    assertTrue(actualStudents.contains(student1()));
    assertTrue(actualStudents.contains(student2()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(TestUtils.MANAGER1_TOKEN);

    UsersApi api = new UsersApi(manager1Client);
    Student actualStudent1 = api.getStudentById(STUDENT1_ID);
    List<Student> actualStudents = api.getStudents();

    assertEquals(student1(), actualStudent1);
    assertTrue(actualStudents.contains(student1()));
    assertTrue(actualStudents.contains(student2()));
  }

  public static Student student1() {
    Student student = new Student();
    student.setId("student1_id");
    student.setFirstName("Ryan");
    student.setLastName("Andria");
    student.setEmail("ryan@hei.school");
    student.setRef("STD21001");
    student.setPhone("0322411123");
    student.setStatus(Student.StatusEnum.ENABLED);
    student.setSex(Student.SexEnum.M);
    student.setBirthDate(LocalDate.parse("2000-01-01"));
    student.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    student.setAddress("Adr 1");
    return student;
  }

  public static Student student2() {
    Student student = new Student();
    student.setId("student2_id");
    student.setFirstName("Two");
    student.setLastName("Student");
    student.setEmail("student2@hei.school");
    student.setRef("STD21002");
    student.setPhone("0322411124");
    student.setStatus(Student.StatusEnum.ENABLED);
    student.setSex(Student.SexEnum.F);
    student.setBirthDate(LocalDate.parse("2000-01-02"));
    student.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student.setAddress("Adr 2");
    return student;
  }
}
