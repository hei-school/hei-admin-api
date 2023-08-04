package school.hei.haapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonParser;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TranscriptApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
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
    void student_read_transcript_ok() throws ApiException {
        ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
        TranscriptApi api = new TranscriptApi(student1Client);

        List<Transcript> actualByStudent1 = api.getStudentTranscripts(STUDENT1_ID, null, null);
        assertEquals(2, actualByStudent1.size());
        assertTrue(actualByStudent1.contains(TestUtils.transcript1()));
    }

    @Test
    void teacher_read_transcript_ok() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        TranscriptApi api = new TranscriptApi(teacher1Client);

        List<Transcript> actualByStudent1 = api.getStudentTranscripts(STUDENT1_ID, null, null);
        assertEquals(2, actualByStudent1.size());
        assertTrue(actualByStudent1.contains(TestUtils.transcript1()));
    }

    @Test
    void create_transcript_raw() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder().build();
        byte[] transcript_byte = "transcript".getBytes();
        HttpRequest.BodyPublisher request_body = HttpRequest.BodyPublishers.ofByteArray(transcript_byte);

        HttpResponse<String> response =  httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + ContextInitializer.SERVER_PORT))
                        .POST(request_body)
                        .setHeader("Content-Type", "application/pdf")
                .build(), HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        StudentTranscriptVersion responseBody = mapper.readValue(response.body(), StudentTranscriptVersion.class);

        assertNotNull(responseBody.getCreatedByUserRole());
        assertNotNull(responseBody.getRef());
        assertNotNull(responseBody.getTranscriptId().length());
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}
