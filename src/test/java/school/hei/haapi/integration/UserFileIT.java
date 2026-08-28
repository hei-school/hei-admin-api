package school.hei.haapi.integration;

import static java.io.File.createTempFile;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.UUID.randomUUID;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.FileType.TRANSCRIPT;
import static school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum.WORKER_STUDENT;
import static school.hei.haapi.integration.conf.ApiAssertions.assertBadRequestException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpRestTemplate;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.FeeTestData.createPendingFee;
import static school.hei.haapi.integration.testData.FileInfoTestData.aUserFile;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.OrganizerTestData.organizerSmith;
import static school.hei.haapi.integration.testData.PaymentTestData.aPayment;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;
import static school.hei.haapi.integration.testData.WorkDocumentTestData.aWorkDocument;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import school.hei.haapi.endpoint.rest.api.FilesApi;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.CrupdateStudent;
import school.hei.haapi.endpoint.rest.model.GenerationReceiptsRequest;
import school.hei.haapi.endpoint.rest.model.Payment;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkDocument;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.FileInfoRepository;
import school.hei.haapi.repository.PaymentRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.WorkDocumentRepository;
import school.hei.haapi.service.utils.ScholarshipCertificateDataProvider;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class UserFileIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @MockBean RestTemplate restTemplateMock;
  @MockBean private ScholarshipCertificateDataProvider scholarshipCertificateDataProvider;

  @Autowired private UserRepository userRepository;
  @Autowired private FileInfoRepository fileInfoRepository;
  @Autowired private FeeRepository feeRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private WorkDocumentRepository workDocumentRepository;

  private User studentAxel;
  private User studentFreddy;
  private User monitorAxel;
  private User teacherToky;
  private User managerHasina;
  private User organizerSmith;

  private FileInfo axelTranscript;
  private FileInfo teacherFile;
  private Fee axelFee;
  private school.hei.haapi.model.Payment axelPayment;
  private WorkDocument axelWorkDocument;

  /** Users the tests create through the API, swept in tearDown. */
  private final List<String> createdUserIds = new ArrayList<>();

  private String axelToken;
  private String monitorToken;
  private String teacherToken;
  private String managerToken;
  private String organizerToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    studentFreddy = userRepository.save(freddy());
    teacherToky = userRepository.save(toky());
    managerHasina = userRepository.save(hasina());
    organizerSmith = userRepository.save(organizerSmith());

    monitorAxel = monitorOfAxel();
    monitorAxel.setMonitors(new ArrayList<>(List.of(studentAxel)));
    monitorAxel = userRepository.save(monitorAxel);

    axelTranscript = fileInfoRepository.save(aUserFile(studentAxel, "transcript1", TRANSCRIPT));
    teacherFile = fileInfoRepository.save(aUserFile(teacherToky, "teacher file", TRANSCRIPT));

    axelFee = feeRepository.save(createPendingFee(studentAxel, 5000, now().plus(30, DAYS)));
    axelPayment =
        paymentRepository.save(
            aPayment(axelFee, Payment.TypeEnum.CASH, 5000, "Comment", now().minus(1, DAYS)));
    axelWorkDocument =
        workDocumentRepository.save(
            aWorkDocument(studentAxel, "work file", WORKER_STUDENT, now().minus(60, DAYS)));
  }

  @BeforeEach
  public void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpRestTemplate(restTemplateMock);
    when(scholarshipCertificateDataProvider.getAcademicYearSentence(any()))
        .thenReturn("test academic year");
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    monitorToken = tokenFor(casdoorAuthServiceMock, monitorAxel);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    organizerToken = tokenFor(casdoorAuthServiceMock, organizerSmith);
  }

  @AfterEach
  void tearDown() {
    workDocumentRepository.deleteById(axelWorkDocument.getId());
    paymentRepository.deleteById(axelPayment.getId());
    feeRepository.deleteById(axelFee.getId());
    fileInfoRepository.deleteAll(
        fileInfoRepository.findAll().stream()
            .filter(f -> f.getUser() != null && ownedUserIds().contains(f.getUser().getId()))
            .toList());
    userRepository.deleteAllById(createdUserIds);
    createdUserIds.clear();
    monitorAxel.setMonitors(new ArrayList<>());
    userRepository.save(monitorAxel);
    userRepository.deleteAll(
        List.of(
            studentAxel, studentFreddy, monitorAxel, teacherToky, managerHasina, organizerSmith));
  }

  private List<String> ownedUserIds() {
    List<String> ids = new ArrayList<>(createdUserIds);
    ids.addAll(
        List.of(
            studentAxel.getId(),
            studentFreddy.getId(),
            teacherToky.getId(),
            managerHasina.getId()));
    return ids;
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private HttpResponse<byte[]> getRaw(String path, String token)
      throws IOException, InterruptedException {
    return HttpClient.newBuilder()
        .build()
        .send(
            HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + localPort + path))
                .GET()
                .header("Authorization", "Bearer " + token)
                .build(),
            HttpResponse.BodyHandlers.ofByteArray());
  }

  private Student createStudentThroughApi(CrupdateStudent toSave) throws ApiException {
    var created =
        new UsersApi(anApiClient(managerToken))
            .createOrUpdateStudents(List.of(toSave), null)
            .getFirst();
    createdUserIds.add(created.getId());
    return created;
  }

  private static CrupdateStudent aCrupdateStudent(LocalDate birthDate, String nic) {
    return new CrupdateStudent()
        .ref("STD" + randomUUID())
        .firstName("Student")
        .lastName("Under test")
        .address("Addr 6")
        .nic(nic)
        .status(ENABLED)
        .email("test+" + randomUUID() + "@hei.school")
        .coordinates(new Coordinates().latitude(20.2325d).longitude(24.5625d))
        .highSchoolOrigin("lycée analamahitsy")
        .entranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"))
        .birthDate(birthDate);
  }

  @Test
  void student_load_other_certificate_ko() {
    var api = new FilesApi(anApiClient(axelToken));

    assertThrowsForbiddenException(
        () -> api.getStudentScholarshipCertificate(studentFreddy.getId()));
  }

  @Test
  void student_load_other_fee_receipt_ko() {
    var api = new PayingApi(anApiClient(axelToken));

    assertThrowsForbiddenException(
        () -> api.getPaidFeeReceipt(studentFreddy.getId(), axelFee.getId(), axelPayment.getId()));
  }

  @Test
  void student_load_fee_receipt_ok() throws IOException, InterruptedException {
    var response =
        getRaw(
            "/students/%s/fees/%s/payments/%s/receipt/raw"
                .formatted(studentAxel.getId(), axelFee.getId(), axelPayment.getId()),
            axelToken);

    System.out.println("DIAG body = " + response.body());
    var asManager =
        getRaw("/students/" + studentAxel.getId() + "/scholarship_certificate/raw", managerToken);
    System.out.println("DIAG manager status = " + asManager.statusCode());
    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
  }

  @Test
  void student_load_certificate_via_http_client_ok()
      throws IOException, InterruptedException, ApiException {
    var adult =
        createStudentThroughApi(aCrupdateStudent(LocalDate.now().minusYears(19), "0000000000000"));
    var adultToken = tokenFor(casdoorAuthServiceMock, adult.getEmail(), User.Role.STUDENT);

    var response =
        getRaw("/students/" + adult.getId() + "/scholarship_certificate/raw", adultToken);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
  }

  @Test
  void student_underage_get_certificate_ok()
      throws IOException, InterruptedException, ApiException {
    var underaged = createStudentThroughApi(aCrupdateStudent(LocalDate.now().minusYears(16), null));
    var underagedToken = tokenFor(casdoorAuthServiceMock, underaged.getEmail(), User.Role.STUDENT);

    var response =
        getRaw("/students/" + underaged.getId() + "/scholarship_certificate/raw", underagedToken);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
  }

  @Test
  void student_missing_nic_get_certificate_ko()
      throws IOException, InterruptedException, ApiException {
    var withoutNic =
        createStudentThroughApi(aCrupdateStudent(LocalDate.now().minusYears(18), null));
    var withoutNicToken =
        tokenFor(casdoorAuthServiceMock, withoutNic.getEmail(), User.Role.STUDENT);

    var response =
        HttpClient.newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + localPort
                                + "/students/"
                                + withoutNic.getId()
                                + "/scholarship_certificate/raw"))
                    .GET()
                    .header("Authorization", "Bearer " + withoutNicToken)
                    .build(),
                HttpResponse.BodyHandlers.ofString());

    assertEquals(FORBIDDEN.value(), response.statusCode());
    assertEquals(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Please complete your information at the"
            + " Administration to be able to get your certificate.\"}",
        response.body());
  }

  @Test
  void monitor_load_followed_student_certificate_via_http_client_ok()
      throws IOException, InterruptedException {
    var response =
        getRaw("/students/" + studentAxel.getId() + "/scholarship_certificate/raw", monitorToken);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
  }

  @Test
  void student_load_other_files_ko() {
    var api = new FilesApi(anApiClient(axelToken));

    assertThrowsForbiddenException(() -> api.getUserFiles(studentFreddy.getId(), 1, 15, null));
  }

  @Test
  void teacher_load_other_files_ko() {
    var api = new FilesApi(anApiClient(teacherToken));

    assertThrowsForbiddenException(() -> api.getUserFiles(studentFreddy.getId(), 1, 15, null));
  }

  @Test
  void teacher_read_own_files_ok() throws ApiException {
    var api = new FilesApi(anApiClient(teacherToken));

    var actual = api.getUserFiles(teacherToky.getId(), 1, 15, null);

    assertEquals(1, actual.size());
    assertEquals(teacherFile.getId(), actual.getFirst().getId());
  }

  @Test
  void organizer_load_other_files_ko() {
    var api = new FilesApi(anApiClient(organizerToken));

    assertThrowsForbiddenException(() -> api.getUserFiles(studentAxel.getId(), 1, 15, null));
  }

  @Test
  void student_read_own_files_ok() throws ApiException {
    var api = new FilesApi(anApiClient(axelToken));

    var documents = api.getUserFiles(studentAxel.getId(), 1, 15, null);

    assertEquals(1, documents.size());
    assertEquals(axelTranscript.getId(), documents.getFirst().getId());
  }

  @Test
  void monitor_read_own_followed_student_ok() throws ApiException {
    var api = new FilesApi(anApiClient(monitorToken));

    var documents = api.getUserFiles(studentAxel.getId(), 1, 15, null);
    var document = api.getUserFilesById(studentAxel.getId(), axelTranscript.getId());

    assertTrue(documents.stream().anyMatch(f -> axelTranscript.getId().equals(f.getId())));
    assertNotNull(document);
  }

  @Test
  void monitor_read_other_student_ko() {
    var api = new FilesApi(anApiClient(monitorToken));

    assertThrowsForbiddenException(() -> api.getUserFiles(studentFreddy.getId(), 1, 15, null));
    assertThrowsForbiddenException(
        () -> api.getUserFilesById(studentFreddy.getId(), axelTranscript.getId()));
  }

  @Test
  void student_read_own_transcripts_ok() throws ApiException {
    var api = new FilesApi(anApiClient(axelToken));

    var documents = api.getUserFiles(studentAxel.getId(), 1, 15, TRANSCRIPT);

    assertEquals(1, documents.size());
    assertEquals(axelTranscript.getId(), documents.getFirst().getId());
  }

  @Test
  void manager_read_student_files_ok() throws ApiException {
    var api = new FilesApi(anApiClient(managerToken));

    var documents = api.getUserFiles(studentAxel.getId(), 1, 15, null);

    assertTrue(documents.stream().anyMatch(f -> axelTranscript.getId().equals(f.getId())));
  }

  @Test
  void manager_create_zip_contain_receipt_ok() throws ApiException {
    var api = new PayingApi(anApiClient(managerToken));

    var zipReceiptsStatistic =
        api.generateFeeReceipts(
            new GenerationReceiptsRequest()
                .from(Instant.parse("2021-11-08T08:25:24.00Z"))
                .to(now()));

    assertNotNull(zipReceiptsStatistic);
  }

  @Test
  void upload_file_with_extension_ko() throws IOException {
    var api = new FilesApi(anApiClient(managerToken));
    var fileToSend = createTempFile("file", "tmp");

    assertBadRequestException(
        "File name must not contain an extension",
        () ->
            api.uploadUserFile(
                studentAxel.getId(),
                TRANSCRIPT,
                "STUDENT/STUDENT_ref/TRANSCRIPT/fileName.extension",
                fileToSend));
  }

  @Test
  void upload_user_file_ok() throws ApiException, IOException {
    var api = new FilesApi(anApiClient(managerToken));
    var fileToSend = createTempFile("file", "tmp");
    var filename = "STUDENT/STUDENT_ref/TRANSCRIPT/fileName";

    var fileInfo = api.uploadUserFile(studentAxel.getId(), TRANSCRIPT, filename, fileToSend);

    assertNotNull(fileInfo);
    assertEquals(filename, fileInfo.getName());
  }

  @Test
  void student_get_workfile_by_id_ok() throws ApiException {
    var api = new FilesApi(anApiClient(managerToken));

    assertNotNull(api.getStudentWorkDocumentsById(studentAxel.getId(), axelWorkDocument.getId()));
  }
}
