package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.FileType.TRANSCRIPT;
import static school.hei.haapi.integration.SchoolFileIT.setUpRestTemplate;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import school.hei.haapi.endpoint.rest.api.FilesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.FileInfo;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class StudentFileIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @MockBean RestTemplate restTemplateMock;
  @Autowired ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
    setUpRestTemplate(restTemplateMock);
  }

  @Test
  void student_load_other_certificate_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    FilesApi api = new FilesApi(student1Client);

    assertThrowsForbiddenException(() -> api.getStudentScholarshipCertificate(STUDENT2_ID));
  }

  @Test
  void student_load_certificate_via_http_client_ok() throws IOException, InterruptedException {
    String STUDENT_CERTIFICATE = "/students/" + STUDENT1_ID + "/scholarship_certificate/raw";
    HttpClient httpClient = HttpClient.newBuilder().build();
    String basePath = "http://localhost:" + localPort;

    HttpResponse<byte[]> response =
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
  void student_load_other_files_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    FilesApi api = new FilesApi(student1Client);

    assertThrowsForbiddenException(() -> api.getStudentFiles(STUDENT2_ID, 1, 15, null));
  }

  @Test
  void student_read_own_files_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    FilesApi api = new FilesApi(student1Client);

    List<FileInfo> documents = api.getStudentFiles(STUDENT1_ID, 1, 15, null);

    assertEquals(2, documents.size());
    assertTrue(documents.contains(file1()));
  }

  @Test
  void student_read_own_transcripts_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    FilesApi api = new FilesApi(student1Client);

    List<FileInfo> documents = api.getStudentFiles(STUDENT1_ID, 1, 15, TRANSCRIPT);

    assertEquals(1, documents.size());
    assertTrue(documents.contains(file1()));
  }

  @Test
  void manager_read_student_files_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    FilesApi api = new FilesApi(manager1Client);

    List<FileInfo> documents = api.getStudentFiles(STUDENT1_ID, 1, 15, null);

    assertEquals(2, documents.size());
    assertTrue(documents.contains(file1()));
  }

  public static FileInfo file1() {
    return new FileInfo()
        .id("file1_id")
        .fileType(TRANSCRIPT)
        .name("transcript1")
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }
}
