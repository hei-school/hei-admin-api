package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.getMockedFileAsByte;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdateManager;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Manager;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.file.BucketConf;
import school.hei.haapi.file.S3Conf;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.service.aws.FileService;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = ManagerIT.ContextInitializer.class)
@AutoConfigureMockMvc
class ManagerIT {

  @MockBean private SentryConf sentryConf;

  @MockBean private CognitoComponent cognitoComponentMock;
  @MockBean BucketConf bucketConf;
  @MockBean
  S3Conf s3Conf;
  @MockBean
  FileService fileService;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, ContextInitializer.SERVER_PORT);
  }

  public static Manager manager1() {
    Manager manager = new Manager();
    manager.setId("manager1_id");
    manager.setFirstName("One");
    manager.setLastName("Manager");
    manager.setEmail("test+manager1@hei.school");
    manager.setRef("MGR21001");
    manager.setPhone("0322411127");
    manager.setStatus(EnableStatus.ENABLED);
    manager.setSex(Sex.M);
    manager.setBirthDate(LocalDate.parse("1890-01-01"));
    manager.setEntranceDatetime(Instant.parse("2021-09-08T08:25:29Z"));
    manager.setAddress("Adr 5");
    manager.setBirthPlace("");
    manager.setNic("");
    return manager;
  }

  public static Manager disabledManager1() {
    return new Manager()
        .id("'manager2_id'")
        .firstName("Disable")
        .lastName("One")
        .email("manager+disable1@hei.school")
        .ref("MGR29001")
        .status(EnableStatus.DISABLED)
        .sex(Sex.M)
        .birthDate(LocalDate.parse("2000-12-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .phone("0322411123")
        .nic("")
        .birthPlace("")
        .address("Adr 1");
  }

  public static Manager suspendedManager1() {
    return new Manager()
        .id("'manager3_id'")
        .firstName("Suspended")
        .lastName("One")
        .email("manager+suspended@hei.school")
        .ref("MGR29003")
        .status(EnableStatus.SUSPENDED)
        .sex(Sex.F)
        .birthDate(LocalDate.parse("2000-12-02"))
        .entranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"))
        .phone("0322411123")
        .nic("")
        .birthPlace("")
        .address("Adr 2");
  }

  public static CrupdateManager someUpdatableManager1() {
    CrupdateManager manager = new CrupdateManager();
    manager.setId("manager1_id");
    manager.setFirstName("One");
    manager.setLastName("Manager");
    manager.setEmail("test+manager1@hei.school");
    manager.setRef("MGR21001");
    manager.setPhone("0322411127");
    manager.setStatus(EnableStatus.ENABLED);
    manager.setSex(Sex.M);
    manager.setBirthDate(LocalDate.parse("1890-01-01"));
    manager.setEntranceDatetime(Instant.parse("2021-09-08T08:25:29Z"));
    manager.setAddress("Adr 5");
    manager.setBirthPlace("");
    manager.setNic("");
    return manager
        .address("Adr 999")
        .sex(Sex.F)
        .lastName("Other last")
        .firstName("Other first")
        .birthDate(LocalDate.parse("2000-01-03"));
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, manager1());
  }

  @Test
  @DirtiesContext
  void manager_update_own_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    Manager updated = api.updateManager(MANAGER_ID, someUpdatableManager1());
    Manager managers = api.getManagerById(MANAGER_ID);
    assertEquals(managers, updated);
  }

  @Test
  void manager_update_own_profile_picture() throws IOException, InterruptedException {
    String MANAGER_ONE_PICTURE_RAW = "/managers/" + MANAGER_ID + "/picture/raw";
    HttpClient httpClient = HttpClient.newBuilder().build();
    String basePath = "http://localhost:" + ManagerIT.ContextInitializer.SERVER_PORT;

    HttpRequest.BodyPublisher body =
        HttpRequest.BodyPublishers.ofByteArray(getMockedFileAsByte("img", ".png"));
    HttpResponse<String> response =
        httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(basePath + MANAGER_ONE_PICTURE_RAW))
                .POST(body)
                .setHeader("Content-Type", "image/png")
                .header("Authorization", "Bearer " + MANAGER1_TOKEN)
                .build(),
            HttpResponse.BodyHandlers.ofString());

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JSR310Module());
    mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    Manager responseBody = mapper.readValue(response.body(), Manager.class);

    assertEquals("MGR21001", responseBody.getRef());
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    UsersApi api = new UsersApi(student1Client);
    assertThrowsForbiddenException(() -> api.getManagerById(MANAGER_ID));
    assertThrowsForbiddenException(() -> api.getManagers(1, 20, null, null));
  }

  @Test
  void teacher_read_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    UsersApi api = new UsersApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.getManagerById(MANAGER_ID));
    assertThrowsForbiddenException(() -> api.getManagers(1, 20, null, null));
  }

  @Test
  void manager_read_own_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

    UsersApi api = new UsersApi(manager1Client);
    Manager actual = api.getManagerById(MANAGER_ID);

    assertEquals(manager1(), actual);
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);

    UsersApi api = new UsersApi(manager1Client);
    List<Manager> managers = api.getManagers(1, 20, null, null);

    assertEquals(3, managers.size());
    assertEquals(manager1(), managers.get(0));
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
