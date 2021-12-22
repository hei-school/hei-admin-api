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
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.isValidUUID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = TeacherIT.ContextInitializer.class)
@AutoConfigureMockMvc
class TeacherIT {

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
  void student_read_ko() {
    ApiClient student1Client = anApiClient(TestUtils.STUDENT1_TOKEN);

    TeachersApi api = new TeachersApi(student1Client);
    assertThrowsApiException(
        () -> api.getTeacherById(TestUtils.TEACHER1_ID),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Students cannot read teachers\"}");
    assertThrowsApiException(
        api::getTeachers,
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Only managers can read all teachers\"}");
  }

  @Test
  void teacher_read_ko() {
    ApiClient teacher1Client = anApiClient(TestUtils.TEACHER1_TOKEN);

    TeachersApi api = new TeachersApi(teacher1Client);
    assertThrowsApiException(
        () -> api.getTeacherById(TestUtils.TEACHER2_ID),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Teachers can only read their own information\"}");
    assertThrowsApiException(
        api::getTeachers,
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Only managers can read all teachers\"}");
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(TestUtils.STUDENT1_TOKEN);

    TeachersApi api = new TeachersApi(student1Client);
    assertThrowsApiException(
        () -> api.createOrUpdateTeachers(List.of()),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Only managers can write teachers\"}");
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TestUtils.TEACHER1_TOKEN);

    TeachersApi api = new TeachersApi(teacher1Client);
    assertThrowsApiException(
        () -> api.createOrUpdateTeachers(List.of()),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Only managers can write teachers\"}");
  }

  @Test
  void teacher_read_own_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TestUtils.TEACHER1_TOKEN);

    TeachersApi api = new TeachersApi(teacher1Client);
    Teacher actual = api.getTeacherById(TestUtils.TEACHER1_ID);

    assertEquals(teacher1(), actual);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(TestUtils.MANAGER1_TOKEN);

    TeachersApi api = new TeachersApi(manager1Client);
    List<Teacher> teachers = api.getTeachers();

    assertTrue(teachers.contains(teacher1()));
    assertTrue(teachers.contains(teacher2()));
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(TestUtils.MANAGER1_TOKEN);
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
  void manager_write_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(TestUtils.MANAGER1_TOKEN);
    TeachersApi api = new TeachersApi(manager1Client);
    Teacher toUpdate = api
        .createOrUpdateTeachers(List.of(aCreatableTeacher()))
        .get(0);
    toUpdate.setLastName("New last name");

    List<Teacher> updated = api.createOrUpdateTeachers(List.of(toUpdate));

    assertEquals(1, updated.size());
    assertEquals(toUpdate, updated.get(0));
  }

  public static Teacher teacher1() {
    Teacher teacher = new Teacher();
    teacher.setId("teacher1_id");
    teacher.setFirstName("One");
    teacher.setLastName("Teacher");
    teacher.setEmail("teacher1@hei.school");
    teacher.setRef("TCR21001");
    teacher.setPhone("0322411125");
    teacher.setStatus(Teacher.StatusEnum.ENABLED);
    teacher.setSex(Teacher.SexEnum.F);
    teacher.setBirthDate(LocalDate.parse("1990-01-01"));
    teacher.setEntranceDatetime(Instant.parse("2021-10-08T08:27:24.00Z"));
    teacher.setAddress("Adr 3");
    return teacher;
  }

  public static Teacher teacher2() {
    Teacher teacher = new Teacher();
    teacher.setId("teacher2_id");
    teacher.setFirstName("Two");
    teacher.setLastName("Teacher");
    teacher.setEmail("teacher2@hei.school");
    teacher.setRef("TCR21002");
    teacher.setPhone("0322411126");
    teacher.setStatus(Teacher.StatusEnum.ENABLED);
    teacher.setSex(Teacher.SexEnum.M);
    teacher.setBirthDate(LocalDate.parse("1990-01-02"));
    teacher.setEntranceDatetime(Instant.parse("2021-10-09T08:28:24Z"));
    teacher.setAddress("Adr 4");
    return teacher;
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
