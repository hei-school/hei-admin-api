package school.hei.haapi.integration;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
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
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = StudentIT.ContextInitializer.class)
@AutoConfigureMockMvc
class StudentIT {

  @MockBean
  private CognitoComponent cognitoComponentMock;

  @MockBean
  private EventBridgeClient eventBridgeClientMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  public static Student aCreatableStudent() {
    Student student = student1();
    student.setId(null);
    student.setRef("STD21_" + randomUUID());
    student.setEmail(randomUUID() + "@hei.school");
    return student;
  }

  public static Student student1() {
    Student student = new Student();
    student.setId("student1_id");
    student.setFirstName("Ryan");
    student.setLastName("Andria");
    student.setEmail("test+ryan@hei.school");
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
    student.setEmail("test+student2@hei.school");
    student.setRef("STD21002");
    student.setPhone("0322411124");
    student.setStatus(Student.StatusEnum.ENABLED);
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
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentById(TestUtils.STUDENT2_ID)
    );
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
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
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    UsersApi api = new UsersApi(student1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateStudents(List.of())
    );
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    UsersApi api = new UsersApi(teacher1Client);
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createOrUpdateStudents(List.of())
    );
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

    UsersApi api = new UsersApi(manager1Client);
    List<Student> actualStudents = api.getStudents();

    assertTrue(actualStudents.contains(student1()));
    assertTrue(actualStudents.contains(student2()));
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    List<Student> toUpdate = api.createOrUpdateStudents(List.of(
        aCreatableStudent(),
        aCreatableStudent()));
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
  void manager_write_update_triggers_userUpserted() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    reset(eventBridgeClientMock);
    when(eventBridgeClientMock.putEvents((PutEventsRequest) any())).thenReturn(
        PutEventsResponse.builder()
            .entries(
                PutEventsResultEntry.builder().eventId("eventId1").build(),
                PutEventsResultEntry.builder().eventId("eventId2").build())
            .build());

    List<Student> created = api.createOrUpdateStudents(List.of(
        aCreatableStudent(),
        aCreatableStudent()));
    Student created0 = created.get(0);
    Student created1 = created.get(1);

    ArgumentCaptor<PutEventsRequest> captor = ArgumentCaptor.forClass(PutEventsRequest.class);
    verify(eventBridgeClientMock, times(1)).putEvents(captor.capture());
    PutEventsRequest actualRequest = captor.getValue();
    List<PutEventsRequestEntry> actualRequestEntries = actualRequest.entries();
    assertEquals(2, actualRequestEntries.size());
    PutEventsRequestEntry requestEntry0 = actualRequestEntries.get(0);
    assertTrue(requestEntry0.detail().contains(created0.getId()));
    assertTrue(requestEntry0.detail().contains(created0.getEmail()));
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
