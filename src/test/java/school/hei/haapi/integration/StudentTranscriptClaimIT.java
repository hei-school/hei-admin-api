package school.hei.haapi.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TranscriptApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = StudentTranscriptClaimIT.ContextInitializer.class)
@AutoConfigureMockMvc
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentTranscriptClaimIT {
  @MockBean
  private SentryConf sentryConf;
  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, StudentTranscriptClaimIT.ContextInitializer.SERVER_PORT);
  }
  static StudentTranscriptClaim studentTranscript1Version1Claim1() {
    return new StudentTranscriptClaim()
            .id(TRANSCRIPT1_VERSION1_CLAIM1_ID)
            .transcriptId(TRANSCRIPT1_ID)
            .transcriptVersionId(TRANSCRIPT1_VERSION1_ID)
            .status(StudentTranscriptClaim.StatusEnum.OPEN)
            .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
            .closedDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
            .reason("Not okay");
  }
  static StudentTranscriptClaim studentTranscript1Version1Claim2() {
    return new StudentTranscriptClaim()
            .id(TRANSCRIPT1_VERSION1_CLAIM2_ID)
            .transcriptId(TRANSCRIPT1_ID)
            .transcriptVersionId(TRANSCRIPT1_VERSION1_ID)
            .status(StudentTranscriptClaim.StatusEnum.CLOSE)
            .creationDatetime(Instant.parse("2021-12-10T08:25:25.00Z"))
            .closedDatetime(Instant.parse("2021-12-10T08:25:25.00Z"))
            .reason("Okay");
  }
  static StudentTranscriptClaim studentTranscript2Version1Claim1() {
    return new StudentTranscriptClaim()
            .id(TRANSCRIPT2_VERSION1_CLAIM1_ID)
            .transcriptId(TRANSCRIPT2_ID)
            .transcriptVersionId(TRANSCRIPT2_VERSION1_ID)
            .status(StudentTranscriptClaim.StatusEnum.CLOSE)
            .creationDatetime(Instant.parse("2022-11-11T08:25:26.00Z"))
            .closedDatetime(Instant.parse("2022-11-11T08:25:26.00Z"))
            .reason("Okay");
  }
  static StudentTranscriptClaim studentTranscript2Version1Claim2() {
    return new StudentTranscriptClaim()
            .id(TRANSCRIPT2_VERSION1_CLAIM2_ID)
            .transcriptId(TRANSCRIPT2_ID)
            .transcriptVersionId(TRANSCRIPT2_VERSION1_ID)
            .status(StudentTranscriptClaim.StatusEnum.CLOSE)
            .creationDatetime(Instant.parse("2022-12-12T08:25:26.00Z"))
            .closedDatetime(Instant.parse("2022-12-12T08:25:26.00Z"))
            .reason("Okay");
  }
  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }
  @Test
  void student_read_all_claim_of_a_transcript_version_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TranscriptApi api = new TranscriptApi(student1Client);

    List<StudentTranscriptClaim> actual = api.getStudentTranscriptClaims(STUDENT1_ID, TRANSCRIPT1_ID, TRANSCRIPT1_VERSION1_ID, 1, 5);

    // check if ordered by creationDatetime DESC
    assertEquals(actual.get(0), studentTranscript1Version1Claim2());
    assertEquals(actual.get(1), studentTranscript1Version1Claim1());
    assertTrue(actual.containsAll(List.of(studentTranscript1Version1Claim1(), studentTranscript1Version1Claim2())));
  }
  @Test
  void student_read_one_claim_of_a_transcript_version_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    TranscriptApi api = new TranscriptApi(student1Client);

    StudentTranscriptClaim expected = api.getStudentClaimOfTranscriptVersion(STUDENT1_ID, TRANSCRIPT1_ID, TRANSCRIPT1_VERSION1_ID, TRANSCRIPT1_VERSION1_CLAIM1_ID);

    assertEquals(expected, studentTranscript1Version1Claim1());
  }

  @Test
  void teacher_read_all_claim_of_a_transcript_version_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TranscriptApi api = new TranscriptApi(teacher1Client);

    List<StudentTranscriptClaim> actual = api.getStudentTranscriptClaims(STUDENT1_ID, TRANSCRIPT2_ID, TRANSCRIPT2_VERSION1_ID, 1, 5);

    // check if ordered by creationDatetime DESC
    assertEquals(actual.get(0), studentTranscript2Version1Claim2());
    assertEquals(actual.get(1), studentTranscript2Version1Claim1());
    assertTrue(actual.containsAll(List.of(studentTranscript2Version1Claim1(), studentTranscript2Version1Claim2())));
  }

  @Test
  void manager_read_all_claim_of_a_transcript_version_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TranscriptApi api = new TranscriptApi(manager1Client);


    List<StudentTranscriptClaim> actual = api.getStudentTranscriptClaims(STUDENT1_ID, TRANSCRIPT1_ID, TRANSCRIPT1_VERSION1_ID, 1, 5);

    assertTrue(actual.containsAll(List.of(studentTranscript1Version1Claim1(), studentTranscript1Version1Claim2())));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
