package school.hei.haapi.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.FileType.DOCUMENT;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration(initializers = SchoolFileIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class SchoolFileIT extends MockedThirdParties {
  @MockBean EventBridgeClient eventBridgeClientMock;
  @Autowired ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void student_read_school_files_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    FilesApi api = new FilesApi(student1Client);

    List<FileInfo> schoolRegulations = api.getSchoolRegulations(1, 15);

    assertTrue(schoolRegulations.contains(schoolFile()));
    assertEquals(1, schoolRegulations.size());
  }

  @Test
  void teacher_read_school_files_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    FilesApi api = new FilesApi(teacher1Client);

    List<FileInfo> schoolRegulations = api.getSchoolRegulations(1, 15);

    assertTrue(schoolRegulations.contains(schoolFile()));
    assertEquals(1, schoolRegulations.size());
  }

  @Test
  void manager_read_school_files_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    FilesApi api = new FilesApi(manager1Client);

    List<FileInfo> schoolRegulations = api.getSchoolRegulations(1, 15);

    assertTrue(schoolRegulations.contains(schoolFile()));
    assertEquals(1, schoolRegulations.size());
  }

  @Test
  void manager_read_school_file_by_id_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    FilesApi api = new FilesApi(manager1Client);

    FileInfo schoolRegulation = api.getSchoolRegulationById("file3_id");

    assertEquals(schoolFile(), schoolRegulation);
  }

  public static FileInfo schoolFile() {
    return new FileInfo()
        .id("file3_id")
        .fileType(DOCUMENT)
        .name("school_file")
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
  }

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, SchoolFileIT.ContextInitializer.SERVER_PORT);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
