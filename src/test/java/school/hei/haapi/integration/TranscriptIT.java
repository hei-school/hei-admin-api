package school.hei.haapi.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TranscriptApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = TranscriptIT.ContextInitializer.class)
@AutoConfigureMockMvc
@Slf4j
class TranscriptIT {
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, TranscriptIT.ContextInitializer.SERVER_PORT);
  }

  static Transcript transcript1() {
    return new Transcript()
        .id(TRANSCRIPT1_ID)
        .studentId(STUDENT1_ID)
            .semester(Transcript.SemesterEnum.S1)
            .academicYear(2021)
            .isDefinitive(false)
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
  }

  static Transcript transcript2() {
    return new Transcript()
        .id(TRANSCRIPT2_ID)
        .studentId(STUDENT1_ID)
            .semester(Transcript.SemesterEnum.S2)
            .academicYear(2022)
            .isDefinitive(false)
        .creationDatetime(Instant.parse("2022-11-10T08:25:25.00Z"));
  }

//  static Transcript transcript3() {
//    return new Transcript()
//            .id(TRANSCRIPT2_ID)
//            .studentId(STUDENT2_ID)
//            .semester(Transcript.SemesterEnum.S6)
//            .academicYear(2023)
//            .isDefinitive(true)
//            .creationDatetime(Instant.parse("2023-11-10T08:25:25.00Z"));
//  }
//
//  static Transcript transcript4() {
//    return new Transcript()
//        .id(TRANSCRIPT4_ID)
//        .studentId(STUDENT2_ID)
//            .semester(Transcript.SemesterEnum.S5)
//            .academicYear(2024)
//            .isDefinitive(true)
//        .creationDatetime(Instant.parse("2024-11-12T08:25:26.00Z"));
//  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  @Order(1)
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TranscriptApi api = new TranscriptApi(student1Client);

    List<Transcript> actual = api.getStudentTranscripts(STUDENT1_ID, 1, 5);

    assertTrue(actual.contains(transcript1()));
    assertTrue(actual.contains(transcript2()));
  }

  @Test
  @Order(2)
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TranscriptApi api = new TranscriptApi(teacher1Client);

    List<Transcript> actual = api.getStudentTranscripts(STUDENT1_ID, 1, 5);

    log.info("HEY We're HERE transcript 1");
    System.out.println(transcript1());

    log.info("SEPARATOR");
    System.out.println("\n\n");
    log.info("HEY We're HERE transcript 1 to string");

    log.info("HEY We're HERE actual");
    System.out.println(actual);

    log.info("SEPARATOR");
    System.out.println("\n\n");
    log.info("HEY We're HERE transcript 2 to string");
    System.out.println(transcript2());
    assertTrue(actual.contains(transcript1()));
    assertTrue(actual.contains(transcript2()));
  }

  @Test
  @Order(3)
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TranscriptApi api = new TranscriptApi(manager1Client);

    List<Transcript> actual = api.getStudentTranscripts(STUDENT1_ID, 1, 5);

    assertTrue(actual.contains(transcript1()));
    assertTrue(actual.contains(transcript2()));
  }

  @Test
  @Order(4)
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TranscriptApi api = new TranscriptApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentTranscripts(STUDENT2_ID, 1, 5));
  }

  @Test
  @Order(5)
  void teacher_read_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TranscriptApi api = new TranscriptApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentTranscripts(STUDENT2_ID, 1, 5));
  }

  @Test
  @Order(6)
  @Rollback
  void manager_write_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TranscriptApi api = new TranscriptApi(manager1Client);

    // put it to definitive
    List<Transcript> actual = api.crudStudentTranscripts(STUDENT1_ID,
        List.of(new Transcript()
              .id(TRANSCRIPT2_ID)
              .studentId(STUDENT1_ID)
              .semester(Transcript.SemesterEnum.S2)
              .academicYear(2022)
              .isDefinitive(true)
              .creationDatetime(Instant.parse("2022-11-10T08:25:25.00Z"))));

    List<Transcript> expected = api.getStudentTranscripts(STUDENT1_ID, 1, 5);
    log.error("PASSED HERE: BEGIN");
    System.out.println(expected);
    log.error("PASSED HERE: END");
    assertTrue(expected.containsAll(actual));
  }

  @Test
  @Order(7)
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TranscriptApi api = new TranscriptApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.crudStudentTranscripts(STUDENT1_ID, List.of()));

  }

  @Test
  @Order(8)
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TranscriptApi api = new TranscriptApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.crudStudentTranscripts(STUDENT1_ID, List.of()));
  }

  @Test
  @Order(9)
  void manager_write_when_student_not_exists_ko() {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TranscriptApi api = new TranscriptApi(manager1Client);
    Transcript toUpdate = new Transcript()
            .id(TRANSCRIPT2_ID)
            .studentId(NON_EXISTANT_STUDENT_ID)
            .semester(Transcript.SemesterEnum.S2)
            .academicYear(2022)
            .isDefinitive(true)
            .creationDatetime(Instant.parse("2022-11-10T08:25:25.00Z"));

    ApiException exception = assertThrows(ApiException.class,
        () -> api.crudStudentTranscripts(STUDENT1_ID, List.of(toUpdate)));

    String exceptionMessage = exception.getMessage();
    System.out.println("Start of exception: \n");
    System.out.println(exceptionMessage);
    System.out.println("End of exception");
    assertTrue(exceptionMessage.contains("Student.id=" + NON_EXISTANT_STUDENT_ID + " is not found"));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
