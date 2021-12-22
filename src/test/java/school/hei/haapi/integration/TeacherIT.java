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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
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
  void student_can_not_read_teachers() {
    ApiClient student1Client = aClient(TestUtils.STUDENT1_TOKEN);

    TeachersApi api = new TeachersApi(student1Client);
    assertThrowsApiException(
        () -> api.getTeacherById(TestUtils.TEACHER1_ID),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Students cannot read teachers\"}");
  }

  @Test
  void teacher1_can_not_read_teacher2() {
    ApiClient teacher1Client = aClient(TestUtils.TEACHER1_TOKEN);

    TeachersApi api = new TeachersApi(teacher1Client);
    assertThrowsApiException(
        () -> api.getTeacherById(TestUtils.TEACHER2_ID),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Teachers can only read their own information\"}");  }

  @Test
  void student_can_not_write_teachers() {
    ApiClient student1Client = aClient(TestUtils.STUDENT1_TOKEN);

    TeachersApi api = new TeachersApi(student1Client);
    assertThrowsApiException(
        () -> api.createOrUpdateTeachers(List.of()),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Only managers can write teachers\"}");
  }

  @Test
  void teacher_can_not_write_teachers() {
    ApiClient teacher1Client = aClient(TestUtils.TEACHER1_TOKEN);

    TeachersApi api = new TeachersApi(teacher1Client);
    assertThrowsApiException(
        () -> api.createOrUpdateTeachers(List.of()),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Only managers can write teachers\"}");
  }

  @Test
  void teacher_can_read_his_own_information() throws ApiException {
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
    expectedTeacher1.setBirthDate(LocalDate.parse("1990-01-01"));
    expectedTeacher1.setEntranceDatetime(Instant.parse("2021-10-08T08:27:24.00Z"));
    expectedTeacher1.setAddress("Adr 3");
    assertEquals(expectedTeacher1, teacher1);
  }

  @Test
  void manager_can_create_teachers() throws ApiException {
    ApiClient manager1Client = aClient(TestUtils.MANAGER1_TOKEN);
    Teacher toCreate = aCreatableTeacher();

    TeachersApi api = new TeachersApi(manager1Client);
    List<Teacher> created = api.createOrUpdateTeachers(List.of(toCreate));

    assertEquals(1, created.size());
    Teacher created0 = created.get(0);
    assertTrue(isValidUUID(created0.getId()));
    toCreate.setId(created0.getId());
    assertEquals(toCreate, created0);
  }

  @Test
  void manager_can_update_teachers() throws ApiException {
    ApiClient manager1Client = aClient(TestUtils.MANAGER1_TOKEN);
    TeachersApi api = new TeachersApi(manager1Client);
    Teacher updateTeacher1 = api
        .createOrUpdateTeachers(List.of(aCreatableTeacher()))
        .get(0);
    updateTeacher1.setLastName("New last name");

    List<Teacher> updated = api.createOrUpdateTeachers(List.of(updateTeacher1));

    assertEquals(1, updated.size());
    assertEquals(updateTeacher1, updated.get(0));
  }

  public static Teacher aCreatableTeacher() {
    Teacher teacher = new Teacher();
    teacher.setFirstName("Some");
    teacher.setLastName("User");
    teacher.setEmail(randomUUID() + "@hei.school");
    teacher.setRef("TCR21-" + randomUUID());
    teacher.setPhone("0332511129");
    teacher.setStatus(Teacher.StatusEnum.ENABLED);
    teacher.setSex(Teacher.SexEnum.M);
    teacher.setBirthDate(LocalDate.parse("2000-01-01"));
    teacher.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    teacher.setAddress("Adr X");
    return teacher;
  }
}
