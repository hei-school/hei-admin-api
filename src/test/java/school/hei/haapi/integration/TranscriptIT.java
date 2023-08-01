package school.hei.haapi.integration;

package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TranscriptApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = TranscriptIT.ContextInitializer.class)
@Testcontainers
@AutoConfigureMockMvc
public class TranscriptIT {
    @MockBean
    private SentryConf sentryConfMock;
    @MockBean
    private CognitoComponent cognitoComponentMock;

    private ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
    }

    @BeforeEach
    void setUp() {
        TestUtils.setUpCognito(cognitoComponentMock);
    }
    @Test
    void getClaimsStudentTranscriptVersion() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TranscriptApi api = new TranscriptApi(student1Client);

        String studentId = "STUDENT1_ID";
        String transcriptId = "TRANSCRIPT1_ID";

        List<StudentTranscriptClaim> claims = api.getStudentTranscriptClaims(studentId, transcriptId);

        assertEquals(200, api.getApiClient().getStatusCode());

        assertTrue(claims.contains(expectedClaim1()));
        assertTrue(claims.contains(expectedClaim2()));
    }
    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}