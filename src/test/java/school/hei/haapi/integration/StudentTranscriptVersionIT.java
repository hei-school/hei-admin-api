package school.hei.haapi.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TranscriptApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = StudentTranscriptVersionIT.ContextInitializer.class)
@AutoConfigureMockMvc
@Slf4j
class StudentTranscriptVersionIT {
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, StudentTranscriptVersionIT.ContextInitializer.SERVER_PORT);
  }
  static StudentTranscriptVersion studentTranscript1Version1() {
    return new StudentTranscriptVersion()
            .id(TRANSCRIPT1_VERSION1_ID)
            .transcriptId(TRANSCRIPT1_ID)
            .ref(1)
            .createdByUserId(MANAGER_ID)
            .createdByUserRole("MANAGER")
            .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
  }
  static StudentTranscriptVersion studentTranscript1Version2() {
    return new StudentTranscriptVersion()
            .id(TRANSCRIPT1_VERSION2_ID)
            .transcriptId(TRANSCRIPT1_ID)
            .ref(2)
            .createdByUserId(MANAGER_ID)
            .createdByUserRole("MANAGER")
            .creationDatetime(Instant.parse("2021-12-10T08:25:25.00Z"));
  }
  static StudentTranscriptVersion studentTranscript2Version1() {
    return new StudentTranscriptVersion()
            .id(TRANSCRIPT2_VERSION1_ID)
            .transcriptId(TRANSCRIPT2_ID)
            .ref(1)
            .createdByUserId(MANAGER_ID)
            .createdByUserRole("MANAGER")
            .creationDatetime(Instant.parse("2022-11-11T08:25:26.00Z"));
  }
  static StudentTranscriptVersion studentTranscript2Version2() {
    return new StudentTranscriptVersion()
            .id(TRANSCRIPT2_VERSION2_ID)
            .transcriptId(TRANSCRIPT2_ID)
            .ref(2)
            .createdByUserId(MANAGER_ID)
            .createdByUserRole("MANAGER")
            .creationDatetime(Instant.parse("2022-12-12T08:25:26.00Z"));
  }
  static StudentTranscriptVersion studentTranscript2Version3() {
    return new StudentTranscriptVersion()
            .id(TRANSCRIPT2_VERSION3_ID)
            .transcriptId(TRANSCRIPT2_ID)
            .ref(3)
            .createdByUserId(MANAGER_ID)
            .createdByUserRole("MANAGER")
            .creationDatetime(Instant.parse("2023-05-12T08:25:26.00Z"));
  }

  static StudentTranscriptVersion studentTranscript3Version1() {
    return new StudentTranscriptVersion()
            .id(TRANSCRIPT3_VERSION1_ID)
            .transcriptId(TRANSCRIPT3_ID)
            .ref(1)
            .createdByUserId(MANAGER_ID)
            .createdByUserRole("MANAGER")
            .creationDatetime(Instant.parse("2023-10-12T08:25:26.00Z"));
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }
  @Test
  void student_read_transcript_version_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TranscriptApi api = new TranscriptApi(student1Client);

    StudentTranscriptVersion expected1 = api.getStudentTranscriptVersion(STUDENT1_ID, TRANSCRIPT1_ID, TRANSCRIPT1_VERSION1_ID);
    StudentTranscriptVersion expected2 = api.getStudentTranscriptVersion(STUDENT1_ID, TRANSCRIPT1_ID, TRANSCRIPT1_VERSION2_ID);

    assertEquals(expected1, studentTranscript1Version1());
    assertEquals(expected2, studentTranscript1Version2());
  }

  @Test
  void teacher_read_transcript_version_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TranscriptApi api = new TranscriptApi(teacher1Client);

    StudentTranscriptVersion expected3 = api.getStudentTranscriptVersion(STUDENT1_ID, TRANSCRIPT2_ID, TRANSCRIPT2_VERSION1_ID);
    StudentTranscriptVersion expected4 = api.getStudentTranscriptVersion(STUDENT1_ID, TRANSCRIPT2_ID, TRANSCRIPT2_VERSION2_ID);

    assertEquals(expected3, studentTranscript2Version1());
    assertEquals(expected4, studentTranscript2Version2());
  }
  @Test
  void manager_read_transcript_version_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TranscriptApi api = new TranscriptApi(manager1Client);

    StudentTranscriptVersion expected5 = api.getStudentTranscriptVersion(STUDENT1_ID, TRANSCRIPT2_ID, TRANSCRIPT2_VERSION3_ID);

    assertEquals(expected5, studentTranscript2Version3());
  }

  @Test
  void student_read_another_student_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TranscriptApi api = new TranscriptApi(student1Client);

    assertThrowsApiException(
            "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
            () -> api.getStudentTranscriptVersion(STUDENT2_ID, TRANSCRIPT3_ID, TRANSCRIPT3_VERSION1_ID));
  }


  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
