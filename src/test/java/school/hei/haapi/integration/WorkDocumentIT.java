package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum.BUSINESS_OWNER;
import static school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum.INTERN_STUDENT;
import static school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum.WORKER_STUDENT;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestFiles.getMockedFile;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.WorkDocumentTestData.aWorkDocument;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.FilesApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.WorkDocumentInfo;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkDocument;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.WorkDocumentRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class WorkDocumentIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private UserRepository userRepository;
  @Autowired private WorkDocumentRepository workDocumentRepository;

  private User studentAxel;
  private User monitorAxel;
  private User managerHasina;

  private WorkDocument workerDocument;
  private WorkDocument businessOwnerDocument;
  private WorkDocument internDocument;

  private String axelToken;
  private String monitorToken;
  private String managerToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    managerHasina = userRepository.save(hasina());

    monitorAxel = monitorOfAxel();
    monitorAxel.setMonitors(new ArrayList<>(List.of(studentAxel)));
    monitorAxel = userRepository.save(monitorAxel);

    workerDocument =
        workDocumentRepository.save(
            aWorkDocument(
                studentAxel,
                "work file",
                WORKER_STUDENT,
                Instant.parse("2021-11-08T08:25:24.00Z")));
    businessOwnerDocument =
        workDocumentRepository.save(
            aWorkDocument(
                studentAxel,
                "business file",
                BUSINESS_OWNER,
                Instant.parse("2020-11-08T08:25:24.00Z")));
    internDocument =
        workDocumentRepository.save(
            aWorkDocument(
                studentAxel,
                "intern file",
                INTERN_STUDENT,
                Instant.parse("2020-11-08T08:25:24.00Z")));
  }

  @BeforeEach
  public void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    monitorToken = tokenFor(casdoorAuthServiceMock, monitorAxel);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
  }

  @AfterEach
  void tearDown() {
    workDocumentRepository.deleteAll(
        List.of(workerDocument, businessOwnerDocument, internDocument));
    monitorAxel.setMonitors(new ArrayList<>());
    userRepository.save(monitorAxel);
    userRepository.deleteAll(List.of(studentAxel, monitorAxel, managerHasina));
  }

  private FilesApi apiAs(String token) {
    return new FilesApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private static List<String> idsOf(List<WorkDocumentInfo> documents) {
    return documents.stream().map(WorkDocumentInfo::getId).toList();
  }

  @Test
  void manager_create_student_work_documents_with_bad_field_ko() {
    var api = apiAs(managerToken);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Commitment begin must be less than commitment"
            + " end\"}",
        () ->
            api.uploadStudentWorkFile(
                studentAxel.getId(),
                "test",
                Instant.parse("2021-11-09T08:25:24.00Z"),
                BUSINESS_OWNER,
                Instant.parse("2021-11-08T08:25:24.00Z"),
                Instant.parse("2021-11-08T08:25:24.00Z"),
                getMockedFile("img", ".png")));
  }

  @Test
  void manager_read_student_work_documents_ok() throws ApiException {
    var workDocuments =
        apiAs(managerToken).getStudentWorkDocuments(studentAxel.getId(), 1, 10, null);

    assertEquals(3, workDocuments.size());
    assertTrue(idsOf(workDocuments).contains(workerDocument.getId()));
    assertTrue(idsOf(workDocuments).contains(businessOwnerDocument.getId()));
    assertTrue(idsOf(workDocuments).contains(internDocument.getId()));
  }

  @Test
  void student_read_own_work_document_ok() throws ApiException {
    var workDocuments = apiAs(axelToken).getStudentWorkDocuments(studentAxel.getId(), 1, 10, null);

    assertEquals(3, workDocuments.size());
    assertTrue(idsOf(workDocuments).contains(workerDocument.getId()));
  }

  @Test
  void monitor_read_own_student_followed_work_document_ok() throws ApiException {
    var workDocuments =
        apiAs(monitorToken).getStudentWorkDocuments(studentAxel.getId(), 1, 10, null);

    assertEquals(3, workDocuments.size());
    assertTrue(idsOf(workDocuments).contains(workerDocument.getId()));
  }

  @Test
  void manager_read_work_documents_by_professional_type_and_student_id() throws ApiException {
    var workDocuments =
        apiAs(managerToken).getStudentWorkDocuments(studentAxel.getId(), 1, 10, BUSINESS_OWNER);

    assertEquals(1, workDocuments.size());
    assertEquals(businessOwnerDocument.getId(), workDocuments.getFirst().getId());
    assertEquals(BUSINESS_OWNER, workDocuments.getFirst().getProfessionalExperience());
  }

  @Test
  void manager_read_work_documents_by_id_ok() throws ApiException {
    var actual =
        apiAs(managerToken)
            .getStudentWorkDocumentsById(studentAxel.getId(), workerDocument.getId());

    assertEquals(workerDocument.getId(), actual.getId());
    assertEquals(workerDocument.getFilename(), actual.getName());
    assertEquals(WORKER_STUDENT, actual.getProfessionalExperience());
  }
}
