package school.hei.haapi.integration;

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
import school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.TranscriptClaim;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;
import static school.hei.haapi.integration.conf.TestUtils.transcript3;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = TranscriptClaimIT.ContextInitializer.class)
@AutoConfigureMockMvc

public class TranscriptClaimIT {

    @MockBean
    private SentryConf sentryConf;
    @MockBean
    private CognitoComponent cognitoComponentMock;


    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, TranscriptClaimIT.ContextInitializer.SERVER_PORT);
    }

    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
    }


    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TranscriptApi api = new TranscriptApi(student1Client);

        StudentTranscriptClaim actualTranscript = api.getStudentClaimOfTranscriptVersion(STUDENT1_ID, TRANSCRIPT1_ID,STUDENT_TRANSCRIPT_VERSION1_ID,STUDENT_TRANSCRIPT_VERSION_CLAIM1_ID);
        List<StudentTranscriptClaim> actual = api.getStudentTranscriptClaims(STUDENT1_ID, TRANSCRIPT1_ID, STUDENT_TRANSCRIPT_VERSION1_ID,1,10);


        assertEquals(studentTranscriptClaim1(), actualTranscript);
        assertTrue(actual.contains(studentTranscriptClaim1()));
        assertTrue(actual.contains(studentTranscriptClaim2()));

    }






    @Test
    void student_read_ko() {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TranscriptApi api = new TranscriptApi(student1Client);

        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.getStudentClaimOfTranscriptVersion(STUDENT2_ID, TRANSCRIPT1_ID,  STUDENT_TRANSCRIPT_VERSION1_ID   ,  STUDENT_TRANSCRIPT_VERSION_CLAIM1_ID));

        assertThrowsApiException(
                "{\"type\":\"404 NOT_FOUND\",\"message\":\"Transcript claim id" + STUDENT_TRANSCRIPT_VERSION_CLAIM3_ID +"not found\"}",
                () -> api.getStudentClaimOfTranscriptVersion(STUDENT1_ID, TRANSCRIPT6_ID,  STUDENT_TRANSCRIPT_VERSION1_ID   ,  STUDENT_TRANSCRIPT_VERSION_CLAIM3_ID));
    }



//    @Test
//    void teacher_read_ok() throws ApiException {
//        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
//        TranscriptApi api = new TranscriptApi(teacher1Client);
//
//        List<StudentTranscriptClaim> actual = api.getStudentClaimOfTranscriptVersion(STUDENT2_ID, TRANSCRIPT1_ID,  STUDENT_TRANSCRIPT_VERSION1_ID , STUDENT_TRANSCRIPT_VERSION_CLAIM1_ID);
//        StudentTranscriptVersion studentTranscriptVersion = api.getStudentTranscriptByVersion(STUDENT1_ID,TRANSCRIPT1_ID,STUDENT_TRANSCRIPT_VERSION1_ID);
//
//        assertEquals(4,actual.size());
//        assertEquals(studentTranscriptVersion1(),studentTranscriptVersion);
//        assertTrue(actual.contains(studentTranscriptVersion1()));
//        assertTrue(actual.contains(studentTranscriptVersion2()));
//        assertTrue(actual.contains(studentTranscriptVersion3()));
//    }








    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
