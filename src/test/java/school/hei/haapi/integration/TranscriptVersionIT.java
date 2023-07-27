package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = TranscriptVersionIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class TranscriptVersionIT {

    @MockBean
    private SentryConf sentryConf;

    @MockBean
    private CognitoComponent cognitoComponentMock;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, TranscriptVersionIT.ContextInitializer.SERVER_PORT);
    }

    @BeforeEach
    void setUp() {setUpCognito(cognitoComponentMock);}

    @Test
    void student_read_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        List<StudentTranscriptVersion> actual = api.getAllTranscriptsVersions("student1_id","transcript1_id",null,null);
        StudentTranscriptVersion actualVersion = api.getStudentTranscriptVersion("student1_id","transcript1_id","transcript_version1_id");

        assertEquals(actual.size(),7);
        assertEquals(studentTranscriptVersion1(),actualVersion);
        assertTrue(actual.contains(studentTranscriptVersion1()));
        assertTrue(actual.contains(studentTranscriptVersion2()));
        assertTrue(actual.contains(studentTranscriptVersion3()));
    }

    @Test
    void manager_read_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TeachingApi api = new TeachingApi(manager1Client);

        List<StudentTranscriptVersion> actual = api.getAllTranscriptsVersions("student1_id","transcript1_id",null,null);
        StudentTranscriptVersion actualVersion = api.getStudentTranscriptVersion("student1_id","transcript1_id","transcript_version1_id");

        assertEquals(actual.size(),7);
        assertEquals(studentTranscriptVersion1(),actualVersion);
        assertTrue(actual.contains(studentTranscriptVersion1()));
        assertTrue(actual.contains(studentTranscriptVersion2()));
        assertTrue(actual.contains(studentTranscriptVersion3()));
    }

    @Test
    void student_read_ko() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TeachingApi api = new TeachingApi(student1Client);

        assertThrowsApiException(
                "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                () -> api.getStudentTranscriptVersion(STUDENT2_ID,TRANSCRIPT2_ID,STUDENT_TRANSCRIPT_VERSION5_ID));

    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
