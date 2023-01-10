package school.hei.haapi.integration;

import com.github.javafaker.Faker;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = StudentIT.ContextInitializer.class)
@AutoConfigureMockMvc
class StudentIT {

  @MockBean
  private SentryConf sentryConf;

  @MockBean
  private CognitoComponent cognitoComponentMock;

  @MockBean
  private EventBridgeClient eventBridgeClientMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  public static Student someCreatableStudent() {
    Student student = student1();
    Faker faker = new Faker();
    student.setId(null);
    student.setFirstName(faker.name().firstName());
    student.setLastName(faker.name().lastName());
    student.setEmail("test+" + randomUUID() + "@hei.school");
    student.setRef("STD21" + (int) (Math.random() * 1_000_000));
    student.setPhone("03" + (int) (Math.random() * 1_000_000_000));
    student.setStatus(EnableStatus.ENABLED);
    student.setSex(Math.random() < 0.3 ? Student.SexEnum.F : Student.SexEnum.M);
    Instant birthday = faker.date().birthday().toInstant();
    int ageOfEntrance = 14 + (int) (Math.random() * 20);
    student.setBirthDate(birthday.atZone(ZoneId.systemDefault()).toLocalDate());
    student.setEntranceDatetime(birthday.plus(365L * ageOfEntrance, ChronoUnit.DAYS));
    student.setAddress(faker.address().fullAddress());
    return student;
  }

  static List<Student> someCreatableStudentList(int nbOfStudent) {
    List<Student> studentList = new ArrayList<>();
    for (int i = 0; i < nbOfStudent; i++) {
      studentList.add(someCreatableStudent());
    }
    return studentList;
  }

  public static Student student1() {
    Student student = new Student();
    student.setId("student1_id");
    student.setFirstName("Ryan");
    student.setLastName("Andria");
    student.setEmail("test+ryan@hei.school");
    student.setRef("STD21001");
    student.setPhone("0322411123");
    student.setStatus(EnableStatus.ENABLED);
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
    student.setEmail("test+student2@hei.school");
    student.setRef("STD21002");
    student.setPhone("0322411124");
    student.setStatus(EnableStatus.ENABLED);
    student.setSex(Student.SexEnum.F);
    student.setBirthDate(LocalDate.parse("2000-01-02"));
    student.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student.setAddress("Adr 2");
    return student;
  }

  public static Student student3() {
    Student student = new Student();
    student.setId("student3_id");
    student.setFirstName("Three");
    student.setLastName("Student");
    student.setEmail("test+student3@hei.school");
    student.setRef("STD21003");
    student.setPhone("0322411124");
    student.setStatus(EnableStatus.ENABLED);
    student.setSex(Student.SexEnum.F);
    student.setBirthDate(LocalDate.parse("2000-01-02"));
    student.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student.setAddress("Adr 2");
    return student;
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
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

    assertThrowsForbiddenException(() -> api.getStudentById(TestUtils.STUDENT2_ID));

    assertThrowsForbiddenException(
        () -> api.getStudents(1, 20, null, null, null));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    UsersApi api = new UsersApi(teacher1Client);
    Student actualStudent1 = api.getStudentById(STUDENT1_ID);

    List<Student> actualStudents = api.getStudents(1, 20, null, null, null);

    assertEquals(student1(), actualStudent1);
    assertTrue(actualStudents.contains(student1()));
    assertTrue(actualStudents.contains(student2()));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);

    assertThrowsForbiddenException(() -> api.createOrUpdateStudents(List.of()));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    UsersApi api = new UsersApi(teacher1Client);

    assertThrowsForbiddenException(() -> api.createOrUpdateStudents(List.of()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents = api.getStudents(1, 20, null, null, null);

    assertTrue(actualStudents.contains(student1()));
    assertTrue(actualStudents.contains(student2()));
  }

  @Test
  void manager_read_by_ref_and_name_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents = api.getStudents(1, 20, student1().getRef(),
        student1().getFirstName(), student1().getLastName());

    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(student1()));
  }

  @Test
  void manager_read_by_ref_ignoring_case_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents = api.getStudents(1, 20, "std21001", null, null);

    assertEquals("STD21001", student1().getRef());
    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(student1()));
  }

  @Test
  void manager_read_by_ref_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents = api.getStudents(1, 20, student1().getRef(), null, null);

    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(student1()));
  }

  @Test
  void manager_read_by_last_name_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents = api.getStudents(1, 20, null, null, student2().getLastName());

    assertEquals(2, actualStudents.size());
    assertTrue(actualStudents.contains(student2()));
    assertTrue(actualStudents.contains(student3()));
  }

  @Test
  void manager_read_by_ref_and_last_name_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents = api.getStudents(1, 20, student2().getRef(),
        null, student2().getLastName());

    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(student2()));
  }

  @Test
  void manager_read_by_ref_and_bad_name_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents = api.getStudents(1, 20, student2().getRef(),
        null, student1().getLastName());

    assertEquals(0, actualStudents.size());
    assertFalse(actualStudents.contains(student1()));
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    List<Student> toUpdate =
        api.createOrUpdateStudents(List.of(someCreatableStudent(), someCreatableStudent()));
    Student toUpdate0 = toUpdate.get(0);
    toUpdate0.setLastName("A new name zero");
    Student toUpdate1 = toUpdate.get(1);
    toUpdate1.setLastName("A new name one");

    List<Student> updated = api.createOrUpdateStudents(toUpdate);

    assertEquals(2, updated.size());
    assertTrue(updated.contains(toUpdate0));
    assertTrue(updated.contains(toUpdate1));
  }

  @Test
  void manager_write_update_rollback_on_event_error() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    Student toCreate = someCreatableStudent();
    reset(eventBridgeClientMock);
    when(eventBridgeClientMock.putEvents((PutEventsRequest) any()))
        .thenThrow(RuntimeException.class);

    assertThrowsApiException(
        "{\"type\":\"500 INTERNAL_SERVER_ERROR\",\"message\":null}",
        () -> api.createOrUpdateStudents(List.of(toCreate)));

    List<Student> actual = api.getStudents(1, 100, null, null, null);
    assertFalse(actual.stream().anyMatch(s -> Objects.equals(toCreate.getEmail(), s.getEmail())));
  }

  @Test
  void manager_write_update_more_than_10_students_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    Student studentToCreate = someCreatableStudent();
    List<Student> listToCreate = someCreatableStudentList(11);
    listToCreate.add(studentToCreate);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Request entries must be <= 10\"}",
        () -> api.createOrUpdateStudents(listToCreate));

    List<Student> actual = api.getStudents(1, 100, null, null, null);
    assertFalse(actual.stream().anyMatch(
        s -> Objects.equals(studentToCreate.getEmail(), s.getEmail())));
  }

  @Test
  void manager_write_update_triggers_userUpserted() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    reset(eventBridgeClientMock);
    when(eventBridgeClientMock.putEvents((PutEventsRequest) any())).thenReturn(
        PutEventsResponse.builder().entries(
                PutEventsResultEntry.builder().eventId("eventId1").build(),
                PutEventsResultEntry.builder().eventId("eventId2").build())
            .build());

    List<Student> created =
        api.createOrUpdateStudents(List.of(someCreatableStudent(), someCreatableStudent()));

    ArgumentCaptor<PutEventsRequest> captor = ArgumentCaptor.forClass(PutEventsRequest.class);
    verify(eventBridgeClientMock, times(1)).putEvents(captor.capture());
    PutEventsRequest actualRequest = captor.getValue();
    List<PutEventsRequestEntry> actualRequestEntries = actualRequest.entries();
    assertEquals(2, actualRequestEntries.size());
    Student created0 = created.get(0);
    PutEventsRequestEntry requestEntry0 = actualRequestEntries.get(0);
    assertTrue(requestEntry0.detail().contains(created0.getId()));
    assertTrue(requestEntry0.detail().contains(created0.getEmail()));
    Student created1 = created.get(1);
    PutEventsRequestEntry requestEntry1 = actualRequestEntries.get(1);
    assertTrue(requestEntry1.detail().contains(created1.getId()));
    assertTrue(requestEntry1.detail().contains(created1.getEmail()));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
