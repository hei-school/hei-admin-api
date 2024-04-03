package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.FileType.WORK_DOCUMENT;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.*;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.FilesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.FileInfo;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = WorkDocumentIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class WorkDocumentIT extends MockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  public static String WORK_DOCUMENT_1_ID = "work_file1_id";

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
  }

  public static FileInfo workDocument1() {
    return new FileInfo()
        .id("work_file1_id")
        .name("work file")
        .fileType(WORK_DOCUMENT)
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
  }

  public static FileInfo workDocument2() {
    return new FileInfo()
        .id("work_file2_id")
        .name("work file 2")
        .fileType(WORK_DOCUMENT)
        .creationDatetime(Instant.parse("2021-12-08T08:25:24.00Z"));
  }

  @Test
  void manager_read_student_work_documents_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    FilesApi api = new FilesApi(manager1Client);

    List<FileInfo> workDocuments = api.getStudentWorkDocuments(STUDENT1_ID, 1, 10);

    assertEquals(1, workDocuments.size());
    assertEquals(workDocument1(), workDocuments.get(0));
  }

  @Test
  void student_read_own_work_document_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    FilesApi api = new FilesApi(student1Client);

    List<FileInfo> workDocuments = api.getStudentWorkDocuments(STUDENT1_ID, 1, 10);

    assertEquals(1, workDocuments.size());
    assertEquals(workDocument1(), workDocuments.get(0));
  }

  @Test
  void manager_read_work_documents_by_id_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    FilesApi api = new FilesApi(manager1Client);

    FileInfo actual = api.getStudentWorkDocumentsById(STUDENT1_ID, WORK_DOCUMENT_1_ID);

    assertEquals(workDocument1(), actual);
  }

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, WorkDocumentIT.ContextInitializer.SERVER_PORT);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
