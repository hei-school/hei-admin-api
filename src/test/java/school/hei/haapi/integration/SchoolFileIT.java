package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpRestTemplate;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.FileInfoTestData.aSchoolFile;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;
import school.hei.haapi.endpoint.rest.api.FilesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FileInfoRepository;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class SchoolFileIT extends FacadeITMockedThirdParties {
  @MockBean EventBridgeClient eventBridgeClientMock;
  @MockBean RestTemplate restTemplateMock;
  @Autowired private UserRepository userRepository;
  @Autowired private FileInfoRepository fileInfoRepository;

  private User studentAxel;
  private User monitorAxel;
  private User teacherToky;
  private User managerHasina;
  private FileInfo schoolRegulation;

  private String axelToken;
  private String monitorToken;
  private String teacherToken;
  private String managerToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    monitorAxel = userRepository.save(monitorOfAxel());
    teacherToky = userRepository.save(toky());
    managerHasina = userRepository.save(hasina());

    schoolRegulation = fileInfoRepository.save(aSchoolFile("school_file"));
  }

  @BeforeEach
  public void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpRestTemplate(restTemplateMock);
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    monitorToken = tokenFor(casdoorAuthServiceMock, monitorAxel);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
  }

  @AfterEach
  void tearDown() {
    fileInfoRepository.deleteById(schoolRegulation.getId());
    userRepository.deleteAll(List.of(studentAxel, monitorAxel, teacherToky, managerHasina));
  }

  private FilesApi apiAs(String token) {
    return new FilesApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private void assertShareLinkOf(String token) throws ApiException {
    var actual = apiAs(token).getSchoolFilesShareLink("/Test-api");

    assertTrue(actual.getPath().contains("/Test-api"));
    assertTrue(actual.getUrl().contains("https://owncloud.example.com"));
  }

  private void assertListsOwnSchoolFile(String token) throws ApiException {
    var schoolRegulations = apiAs(token).getSchoolRegulations(1, 100);

    assertTrue(
        schoolRegulations.stream().anyMatch(f -> schoolRegulation.getId().equals(f.getId())));
  }

  @Test
  void manager_get_share_link() throws ApiException {
    assertShareLinkOf(managerToken);
  }

  @Test
  void student_get_share_link() throws ApiException {
    assertShareLinkOf(axelToken);
  }

  @Test
  void teacher_get_share_link() throws ApiException {
    assertShareLinkOf(teacherToken);
  }

  @Test
  void student_read_school_files_ok() throws ApiException {
    assertListsOwnSchoolFile(axelToken);
  }

  @Test
  void monitor_read_school_files_ok() throws ApiException {
    assertListsOwnSchoolFile(monitorToken);
  }

  @Test
  void teacher_read_school_files_ok() throws ApiException {
    assertListsOwnSchoolFile(teacherToken);
  }

  @Test
  void manager_read_school_files_ok() throws ApiException {
    assertListsOwnSchoolFile(managerToken);
  }

  @Test
  void manager_read_school_file_by_id_ok() throws ApiException {
    var actual = apiAs(managerToken).getSchoolRegulationById(schoolRegulation.getId());

    assertEquals(schoolRegulation.getId(), actual.getId());
    assertEquals(schoolRegulation.getName(), actual.getName());
  }
}
