package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

import java.util.List;
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

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = TranscriptVersionIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class TranscriptVersionIT {

  @MockBean private SentryConf sentryConf;
  @MockBean private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, TranscriptVersionIT.ContextInitializer.SERVER_PORT);
  }

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void student_read_others_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TranscriptApi api = new TranscriptApi((student1Client));

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            api.getStudentTranscriptByVersion(
                STUDENT2_ID, TRANSCRIPT2_ID, STUDENT_TRANSCRIPT_VERSION5_ID));

    assertThrowsApiException(
        "{\"type\":\"404 NOT_FOUND\",\"message\":\"TranscriptVersion with id transcript_version8_id"
            + " not found\"}",
        () ->
            api.getStudentTranscriptByVersion(
                STUDENT1_ID, TRANSCRIPT4_ID, STUDENT_TRANSCRIPT_VERSION8_ID));
  }

  @Test
  void student_read_self_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TranscriptApi api = new TranscriptApi(student1Client);

    List<StudentTranscriptVersion> actual =
        api.getTranscriptsVersions("student1_id", "transcript1_id", 1, 15);
    StudentTranscriptVersion studentTranscriptVersion =
        api.getStudentTranscriptByVersion(
            STUDENT1_ID, TRANSCRIPT1_ID, STUDENT_TRANSCRIPT_VERSION1_ID);

    assertEquals(4, actual.size());
    assertEquals(studentTranscriptVersion1(), studentTranscriptVersion);
    assertTrue(actual.contains(studentTranscriptVersion1()));
    assertTrue(actual.contains(studentTranscriptVersion2()));
    assertTrue(actual.contains(studentTranscriptVersion3()));
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TranscriptApi api = new TranscriptApi(manager1Client);

    List<StudentTranscriptVersion> actual =
        api.getTranscriptsVersions("student1_id", "transcript1_id", 1, 15);
    StudentTranscriptVersion studentTranscriptVersion =
        api.getStudentTranscriptByVersion(
            STUDENT1_ID, TRANSCRIPT1_ID, STUDENT_TRANSCRIPT_VERSION1_ID);

    assertEquals(4, actual.size());
    assertEquals(studentTranscriptVersion1(), studentTranscriptVersion);
    assertTrue(actual.contains(studentTranscriptVersion1()));
    assertTrue(actual.contains(studentTranscriptVersion2()));
    assertTrue(actual.contains(studentTranscriptVersion3()));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TranscriptApi api = new TranscriptApi(teacher1Client);

    List<StudentTranscriptVersion> actual =
        api.getTranscriptsVersions("student1_id", "transcript1_id", 1, 15);
    StudentTranscriptVersion studentTranscriptVersion =
        api.getStudentTranscriptByVersion(
            STUDENT1_ID, TRANSCRIPT1_ID, STUDENT_TRANSCRIPT_VERSION1_ID);

    assertEquals(4, actual.size());
    assertEquals(studentTranscriptVersion1(), studentTranscriptVersion);
    assertTrue(actual.contains(studentTranscriptVersion1()));
    assertTrue(actual.contains(studentTranscriptVersion2()));
    assertTrue(actual.contains(studentTranscriptVersion3()));
  }

  @Test
  void student_read_latest_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TranscriptApi api = new TranscriptApi(student1Client);

    StudentTranscriptVersion studentTranscriptVersion =
        api.getStudentTranscriptByVersion(STUDENT1_ID, TRANSCRIPT1_ID, "latest");
    assertEquals(studentTranscriptVersion4(), studentTranscriptVersion);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
