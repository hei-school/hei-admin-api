package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TranscriptApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.integration.conf.TranscriptApiMultipart;
import school.hei.haapi.service.aws.S3Service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MULTIPART_FILE_UPLOADED;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TRANSCRIPT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.conf.TestUtils.transcript1;
import static school.hei.haapi.integration.conf.TestUtils.transcript2;
import static school.hei.haapi.integration.conf.TestUtils.transcript3;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = TranscriptVersionRawIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class TranscriptVersionRawIT {
    @MockBean
    private SentryConf sentryConf;
    @MockBean
    private CognitoComponent cognitoComponentMock;
    @MockBean
    private S3Service s3ServiceMock;

    @Autowired
    private MockMvc mockMvc;

    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, TranscriptVersionRawIT.ContextInitializer.SERVER_PORT);
    }

    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
    }

    @BeforeEach
    void setUpAwsS3(){ setUpS3Service(s3ServiceMock);}

    @Test
    void teacher_write_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TranscriptApi api = new TranscriptApi(student1Client);

        Transcript actualTranscript = api.getStudentTranscriptById(STUDENT1_ID, TRANSCRIPT1_ID);
        List<Transcript> actual = api.getStudentTranscripts(STUDENT1_ID, 1, 10);

        assertEquals(transcript1(), actualTranscript);
        assertTrue(actual.contains(transcript1()));
        assertTrue(actual.contains(transcript2()));
        assertTrue(actual.contains(transcript3()));
    }

    @Test
    void manager_write_ok() throws ApiException {
        ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
        TranscriptApiMultipart api = new TranscriptApiMultipart(manager1Client);

        //StudentTranscriptVersion actualTranscript = api.putStudentTranscriptVersionPdf(STUDENT1_ID,TRANSCRIPT1_ID, MULTIPART_FILE_UPLOADED);
        //assertEquals(transcriptVersion1(), actualTranscript);
    }


        @Test
        void student_write_ko() {
            ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
            TranscriptApiMultipart api = new TranscriptApiMultipart(student1Client);

            assertThrowsApiException(
                    "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
                    () -> api.putStudentTranscriptVersionPdf(STUDENT1_ID,TRANSCRIPT1_ID, MULTIPART_FILE_UPLOADED));
        }


    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
