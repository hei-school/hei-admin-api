package school.hei.haapi.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.StudentFileIT.ContextInitializer.SERVER_PORT;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.FilesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Document;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = StudentFileIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class StudentFileIT extends MockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void student_load_other_certificate_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    FilesApi api = new FilesApi(student1Client);

    assertThrowsForbiddenException(() -> api.getStudentScholarshipCertificate(STUDENT2_ID));
  }

  @Test
  void student_load_certificate_via_http_client_ok() throws IOException, InterruptedException {
    String STUDENT_CERTIFICATE = "/students/" + STUDENT1_ID + "/scholarship_certificate/raw";
    HttpClient httpClient = HttpClient.newBuilder().build();
    String basePath = "http://localhost:" + SERVER_PORT;

    HttpResponse response =
        httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(basePath + STUDENT_CERTIFICATE))
                .GET()
                .header("Authorization", "Bearer " + STUDENT1_TOKEN)
                .build(),
            HttpResponse.BodyHandlers.ofByteArray());

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
    assertNotNull(response);
  }

  @Test
  void manager_load_via_http_client_certificate_ok() throws IOException, InterruptedException {
    String STUDENT_CERTIFICATE = "/students/" + STUDENT2_ID + "/scholarship_certificate/raw";
    HttpClient httpClient = HttpClient.newBuilder().build();
    String basePath = "http://localhost:" + SERVER_PORT;

    HttpResponse response =
        httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(basePath + STUDENT_CERTIFICATE))
                .GET()
                .header("Authorization", "Bearer " + MANAGER1_TOKEN)
                .build(),
            HttpResponse.BodyHandlers.ofByteArray());

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
    assertNotNull(response);
  }

  @Test
  void manager_upload_student_file() throws IOException, InterruptedException {
    String STUDENT_FILE =
        "/students/" + STUDENT1_ID + "/files/raw?file_type=TRANSCRIPT&file_name=transcript";
    HttpClient httpClient = HttpClient.newBuilder().build();
    String basePath = "http://localhost:" + SERVER_PORT;

    HttpResponse response =
        httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(basePath + STUDENT_FILE))
                .GET()
                .header("Authorization", "Bearer " + MANAGER1_TOKEN)
                .build(),
            HttpResponse.BodyHandlers.ofByteArray());

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
    assertNotNull(response);
  }

  @Test
  void student_load_other_files_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    FilesApi api = new FilesApi(student1Client);

    assertThrowsForbiddenException(() -> api.getStudentFiles(STUDENT2_ID));
  }

  @Test
  void student_read_own_files_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    FilesApi api = new FilesApi(student1Client);

    List<Document> documents = api.getStudentFiles(STUDENT1_ID);
    assertEquals(2, documents.size());
    assertEquals(file1(), documents.get(0));
  }

  @Test
  void manager_read_student_files_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    FilesApi api = new FilesApi(manager1Client);

    List<Document> documents = api.getStudentFiles(STUDENT1_ID);
    assertEquals(2, documents.size());
    assertEquals(file1(), documents.get(0));
  }

  public static Document file1() {
    return new Document()
        .id("file1_id")
        .fileType(FileType.TRANSCRIPT)
        .name("transcript1")
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
  }

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, StudentFileIT.ContextInitializer.SERVER_PORT);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
