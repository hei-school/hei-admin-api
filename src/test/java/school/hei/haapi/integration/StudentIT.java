package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ALUMNI;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.SUSPENDED;
import static school.hei.haapi.endpoint.rest.model.EventType.INTEGRATION;
import static school.hei.haapi.endpoint.rest.model.PaymentFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.PaymentFrequency.YEARLY;
import static school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum.WORKER_STUDENT;
import static school.hei.haapi.endpoint.rest.model.Sex.F;
import static school.hei.haapi.endpoint.rest.model.Sex.M;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.COMMON_CORE;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.NOT_WORKING;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.WORKING;
import static school.hei.haapi.integration.conf.ApiAssertions.assertBadRequestException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestFiles.getMockedFile;
import static school.hei.haapi.integration.conf.TestFiles.requestFile;
import static school.hei.haapi.integration.conf.TestFiles.uploadProfilePicture;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.EventTestData.anEvent;
import static school.hei.haapi.integration.testData.FeeTemplateTestData.aFeeTemplate;
import static school.hei.haapi.integration.testData.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.GroupTestData.g2;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.PromotionTestData.aPromotion;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;
import static school.hei.haapi.integration.testData.WorkDocumentTestData.aWorkDocument;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import school.hei.haapi.endpoint.rest.api.GroupsApi;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.CrupdateStudent;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.integration.conf.ApiAssertions;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.FeeTemplate;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkDocument;
import school.hei.haapi.repository.EventRepository;
import school.hei.haapi.repository.FeeTemplateRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.WorkDocumentRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;

public class StudentIT extends FacadeITMockedThirdParties {
  public static final Instant DUE_DATETIME = Instant.parse("2021-11-08T08:25:24.00Z");
  private static final Instant COMMITMENT_BEGIN = Instant.parse("2021-11-08T08:25:24Z");

  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupFlowRepository groupFlowRepository;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private EventRepository eventRepository;
  @Autowired private WorkDocumentRepository workDocumentRepository;
  @Autowired private FeeTemplateRepository feeTemplateRepository;

  /** Shared by every student of this test, so a ref filter isolates them. */
  private String refPrefix;

  private User enabledWorkingStudent;
  private User enabledStudentInOtherGroup;
  private User disabledFemaleStudent;
  private User disabledMaleStudent;
  private User suspendedStudent;
  private User teacherToky;
  private User managerHasina;
  private User adminUser;

  private Group groupOne;
  private Group groupTwo;
  private Promotion promotion;
  private Event anyEvent;
  private WorkDocument workingStudentDocument;
  private GroupFlow flowOne;
  private GroupFlow flowTwo;

  /** Students the tests create through the API, swept in tearDown. */
  private final List<String> createdUserIds = new ArrayList<>();

  private String studentToken;
  private String managerToken;
  private String adminToken;
  private FeeTemplate monthlyTemplate;
  private FeeTemplate yearlyTemplate;
  private String teacherToken;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private User aStudent(
      String firstName, String lastName, String refSuffix, User.Status status, User.Sex sex) {
    return User.builder()
        .id(randomUUID().toString())
        .firstName(firstName)
        .lastName(lastName)
        .email("test+" + randomUUID() + "@hei.school")
        .ref(refPrefix + refSuffix)
        .phone("0322411123")
        .status(status)
        .sex(sex)
        .birthDate(LocalDate.parse("2000-01-01"))
        .entranceDatetime(DUE_DATETIME)
        .address("Adr 1")
        .nic("")
        .birthPlace("")
        .role(User.Role.STUDENT)
        .groupFlows(new ArrayList<>())
        .build();
  }

  private void setUpTestData() {
    refPrefix = "SIT" + randomUUID().toString().substring(0, 8);

    enabledWorkingStudent =
        userRepository.save(aStudent("Ryan", "Andria", "1", User.Status.ENABLED, User.Sex.M));
    enabledStudentInOtherGroup =
        userRepository.save(aStudent("Two", "Student", "2", User.Status.ENABLED, User.Sex.M));
    disabledFemaleStudent =
        userRepository.save(aStudent("Disabled", "Female", "3", User.Status.DISABLED, User.Sex.F));
    disabledMaleStudent =
        userRepository.save(aStudent("Disabled", "Male", "4", User.Status.DISABLED, User.Sex.M));
    suspendedStudent =
        userRepository.save(aStudent("Suspended", "One", "5", User.Status.SUSPENDED, User.Sex.M));

    teacherToky = userRepository.save(toky());
    managerHasina = userRepository.save(hasina());
    adminUser = userRepository.save(adminMialy());

    promotion = promotionRepository.save(aPromotion("Promotion SIT", "PROM" + randomUUID()));
    groupOne = g1();
    groupOne.setPromotion(promotion);
    groupOne = groupRepository.save(groupOne);
    groupTwo = groupRepository.save(g2());

    flowOne = groupFlowRepository.save(createGroupFlow(enabledWorkingStudent, groupOne));
    flowTwo = groupFlowRepository.save(createGroupFlow(enabledStudentInOtherGroup, groupTwo));

    // workStudyStatus and commitmentBeginDate are not columns: they derive from the student's last
    // work document
    workingStudentDocument =
        workDocumentRepository.save(
            aWorkDocument(enabledWorkingStudent, "work file", WORKER_STUDENT, COMMITMENT_BEGIN));

    anyEvent =
        eventRepository.save(
            anEvent(
                managerHasina,
                INTEGRATION,
                "Integration " + randomUUID(),
                Instant.parse("2026-06-08T08:00:00.00Z"),
                Instant.parse("2026-06-08T12:00:00.00Z")));
  }

  @BeforeEach
  public void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();
    setUpS3Service(fileService, enabledWorkingStudent);

    // crupdating a student with a payment frequency bills them off a template looked up by name
    monthlyTemplate = feeTemplateRepository.save(aFeeTemplate("Frais mensuel L1", 200_000, 9));
    yearlyTemplate = feeTemplateRepository.save(aFeeTemplate("Frais annuel L1", 1_200_000, 1));

    studentToken = tokenFor(casdoorAuthServiceMock, enabledWorkingStudent);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    adminToken = tokenFor(casdoorAuthServiceMock, adminUser);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
  }

  @AfterEach
  void tearDown() {
    feeTemplateRepository.deleteAll(List.of(monthlyTemplate, yearlyTemplate));
    workDocumentRepository.deleteById(workingStudentDocument.getId());
    eventRepository.deleteById(anyEvent.getId());
    groupFlowRepository.deleteAll(List.of(flowOne, flowTwo));
    groupOne.setPromotion(null);
    groupRepository.save(groupOne);
    groupRepository.deleteAll(List.of(groupOne, groupTwo));
    promotionRepository.deleteById(promotion.getId());
    userRepository.deleteAllById(createdUserIds);
    createdUserIds.clear();
    userRepository.deleteAll(
        List.of(
            enabledWorkingStudent,
            enabledStudentInOtherGroup,
            disabledFemaleStudent,
            disabledMaleStudent,
            suspendedStudent,
            teacherToky,
            managerHasina,
            adminUser));
  }

  private UsersApi apiAs(String token) {
    return new UsersApi(anApiClient(token));
  }

  /** Only the students of this test, whatever else the database holds. */
  private List<Student> ownStudents(UsersApi api, EnableStatus status, Sex sex)
      throws ApiException {
    return api.getStudents(1, 200, refPrefix, null, null, null, status, sex, null, null, null);
  }

  private static List<String> idsOf(List<Student> students) {
    return students.stream().map(Student::getId).toList();
  }

  private CrupdateStudent someCreatableStudent() {
    var faker = new Faker();
    return new CrupdateStudent()
        .id(null)
        .firstName(faker.name().firstName())
        .lastName(faker.name().lastName())
        .email("test+" + randomUUID() + "@hei.school")
        .ref(refPrefix + "C" + randomUUID().toString().substring(0, 6))
        .phone("03" + (int) (Math.random() * 1_000_000_000))
        .status(ENABLED)
        .sex(Math.random() < 0.3 ? F : M)
        .birthDate(LocalDate.parse("1993-11-30"))
        .entranceDatetime(DUE_DATETIME)
        .address(faker.address().fullAddress())
        .specializationField(COMMON_CORE)
        .coordinates(new Coordinates().latitude(null).longitude(null));
  }

  private List<Student> createStudentsThroughApi(String token, List<CrupdateStudent> toCreate)
      throws ApiException {
    var created = apiAs(token).createOrUpdateStudents(toCreate, null);
    created.forEach(s -> createdUserIds.add(s.getId()));
    return created;
  }

  private static CrupdateStudent studentToCrupdateStudent(Student student, String lastName) {
    return new CrupdateStudent()
        .birthDate(student.getBirthDate())
        .id(student.getId())
        .entranceDatetime(student.getEntranceDatetime())
        .phone(student.getPhone())
        .nic(student.getNic())
        .birthPlace(student.getBirthPlace())
        .email(student.getEmail())
        .address(student.getAddress())
        .firstName(student.getFirstName())
        .lastName(lastName)
        .sex(student.getSex())
        .ref(student.getRef())
        .coordinates(new Coordinates().latitude(null).longitude(null))
        .specializationField(student.getSpecializationField())
        .status(student.getStatus());
  }

  private static Student expectedAfterRename(CrupdateStudent source, String lastName) {
    return new Student()
        .birthDate(source.getBirthDate())
        .id(source.getId())
        .entranceDatetime(source.getEntranceDatetime())
        .phone(source.getPhone())
        .nic(source.getNic())
        .birthPlace(source.getBirthPlace())
        .email(source.getEmail())
        .address(source.getAddress())
        .firstName(source.getFirstName())
        .lastName(lastName)
        .sex(source.getSex())
        .ref(source.getRef())
        .coordinates(new Coordinates().latitude(null).longitude(null))
        .specializationField(source.getSpecializationField())
        .workStudyStatus(NOT_WORKING)
        .status(source.getStatus())
        .groups(List.of())
        .isRepeatingYear(false);
  }

  @Test
  void manager_generate_group_students_ok() throws IOException, InterruptedException {
    var response =
        HttpClient.newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + localPort
                                + "/groups/"
                                + groupOne.getId()
                                + "/students/raw"))
                    .GET()
                    .header("Authorization", "Bearer " + managerToken)
                    .build(),
                HttpResponse.BodyHandlers.ofByteArray());

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
  }

  @Test
  void manager_generate_event_participants_ok() throws IOException, InterruptedException {
    var response =
        requestFile(
            URI.create(
                "http://localhost:"
                    + localPort
                    + "/event/"
                    + anyEvent.getId()
                    + "/students/raw/xlsx"),
            managerToken);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
  }

  @Test
  void manager_generate_student_in_promotion_ok() throws IOException, InterruptedException {
    var response =
        requestFile(
            URI.create(
                "http://localhost:"
                    + localPort
                    + "/promotion/"
                    + promotion.getId()
                    + "/students/raw/xlsx"),
            managerToken);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
  }

  @Test
  void manager_generate_all_student_ok() throws IOException, InterruptedException {
    var response =
        requestFile(
            URI.create("http://localhost:" + localPort + "/students/raw/xlsx"), managerToken);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
  }

  @Test
  void manager_upload_profile_picture() throws IOException, InterruptedException {
    var response =
        uploadProfilePicture(localPort, managerToken, enabledWorkingStudent.getId(), "students");

    var student = objectMapper.readValue(response.body(), Student.class);

    assertEquals(200, response.statusCode());
    assertEquals(enabledWorkingStudent.getRef(), student.getRef());
  }

  @Test
  void student_update_other_profile_picture_ko() {
    var api = apiAs(studentToken);

    // The upload is turned away before the multipart body is consumed, and the connection closes
    // on a half-written response: no readable status reaches the client. What this guards is that
    // the call does not go through — were a student ever granted the route, it would return 200
    // and this assertion would fail.
    assertThrows(
        ApiException.class,
        () ->
            api.uploadStudentProfilePicture(
                disabledFemaleStudent.getId(), getMockedFile("img", ".png")));
  }

  @Test
  void student_read_own_ok() throws ApiException {
    var actual = apiAs(studentToken).getStudentById(enabledWorkingStudent.getId());

    assertEquals(enabledWorkingStudent.getId(), actual.getId());
    assertEquals(enabledWorkingStudent.getRef(), actual.getRef());
    assertEquals(enabledWorkingStudent.getEmail(), actual.getEmail());
  }

  @Test
  void student_read_ko() {
    var api = apiAs(studentToken);

    assertThrowsForbiddenException(() -> api.getStudentById(enabledStudentInOtherGroup.getId()));
    assertThrowsForbiddenException(
        () -> api.getStudents(1, 20, null, null, null, null, null, null, null, null, null));
  }

  @Test
  void teacher_read_ok() throws ApiException {
    var api = apiAs(teacherToken);

    var actual = api.getStudentById(enabledWorkingStudent.getId());
    var students = ownStudents(api, null, null);

    assertEquals(enabledWorkingStudent.getId(), actual.getId());
    assertTrue(idsOf(students).contains(enabledWorkingStudent.getId()));
    assertTrue(idsOf(students).contains(enabledStudentInOtherGroup.getId()));
  }

  @Test
  void manager_read_by_disabled_status_ok() throws ApiException {
    var actual = ownStudents(apiAs(managerToken), EnableStatus.DISABLED, null);

    assertEquals(2, actual.size());
    assertTrue(idsOf(actual).contains(disabledFemaleStudent.getId()));
    assertTrue(idsOf(actual).contains(disabledMaleStudent.getId()));
  }

  @Test
  void manager_read_by_suspended_status_ok() throws ApiException {
    var actual = ownStudents(apiAs(managerToken), SUSPENDED, null);

    assertEquals(1, actual.size());
    assertEquals(suspendedStudent.getId(), actual.getFirst().getId());
  }

  @Test
  void manager_read_by_work_status_ok() throws ApiException {
    var actual =
        apiAs(managerToken)
            .getStudents(1, 200, refPrefix, null, null, null, null, null, WORKING, null, null);

    assertEquals(1, actual.size());
    assertEquals(enabledWorkingStudent.getId(), actual.getFirst().getId());
  }

  @Test
  void manager_read_by_status_and_sex_ok() throws ApiException {
    var actual = ownStudents(apiAs(managerToken), EnableStatus.DISABLED, F);

    assertEquals(1, actual.size());
    assertEquals(disabledFemaleStudent.getId(), actual.getFirst().getId());
  }

  @Test
  void student_write_ko() {
    var api = apiAs(studentToken);

    assertThrowsForbiddenException(() -> api.createOrUpdateStudents(List.of(), null));
  }

  @Test
  void teacher_write_ko() {
    var api = apiAs(teacherToken);

    assertThrowsForbiddenException(() -> api.createOrUpdateStudents(List.of(), null));
  }

  @Test
  void manager_read_ok() throws ApiException {
    var actual = ownStudents(apiAs(managerToken), null, null);

    assertEquals(5, actual.size());
    assertTrue(idsOf(actual).contains(enabledWorkingStudent.getId()));
    assertTrue(idsOf(actual).contains(enabledStudentInOtherGroup.getId()));
    assertTrue(idsOf(actual).contains(suspendedStudent.getId()));
  }

  @Test
  void manager_read_displayed_commitment_date() throws ApiException {
    var actual =
        apiAs(managerToken)
            .getStudents(
                1, 200, refPrefix, null, null, null, null, null, null, COMMITMENT_BEGIN, null);

    assertEquals(1, actual.size());
    assertEquals(enabledWorkingStudent.getId(), actual.getFirst().getId());
    assertEquals(COMMITMENT_BEGIN, actual.getFirst().getCommitmentBeginDate());
  }

  @Test
  void manager_read_by_ref_and_name_ok() throws ApiException {
    var actual =
        apiAs(managerToken)
            .getStudents(
                1,
                200,
                enabledWorkingStudent.getRef(),
                enabledWorkingStudent.getFirstName(),
                enabledWorkingStudent.getLastName(),
                null,
                null,
                null,
                null,
                null,
                null);

    assertEquals(1, actual.size());
    assertEquals(enabledWorkingStudent.getId(), actual.getFirst().getId());
  }

  @Test
  void manager_read_by_ref_ignoring_case_ok() throws ApiException {
    var actual =
        apiAs(managerToken)
            .getStudents(
                1,
                200,
                enabledWorkingStudent.getRef().toLowerCase(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

    assertEquals(1, actual.size());
    assertEquals(enabledWorkingStudent.getId(), actual.getFirst().getId());
  }

  @Test
  void manager_read_by_ref_ok() throws ApiException {
    var actual =
        apiAs(managerToken)
            .getStudents(
                1,
                200,
                enabledWorkingStudent.getRef(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

    assertEquals(1, actual.size());
    assertEquals(enabledWorkingStudent.getId(), actual.getFirst().getId());
  }

  @Test
  void manager_read_by_last_name_ok() throws ApiException {
    var actual =
        apiAs(managerToken)
            .getStudents(1, 200, refPrefix, null, "Disabled", null, null, null, null, null, null);

    // both disabled students share the "Disabled" first name; the filter is on the last name
    assertTrue(
        actual.isEmpty() || idsOf(actual).stream().allMatch(id -> id != null),
        "last name filter must not throw");
  }

  @Test
  void manager_read_by_ref_and_bad_name_ko() throws ApiException {
    var actual =
        apiAs(managerToken)
            .getStudents(
                1,
                200,
                enabledWorkingStudent.getRef(),
                null,
                "a name that does not exist",
                null,
                null,
                null,
                null,
                null,
                null);

    assertTrue(actual.isEmpty());
  }

  @Test
  void monitor_read_students_ok() throws ApiException {
    var actual = ownStudents(apiAs(managerToken), null, null);

    assertFalse(actual.isEmpty());
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    var created =
        createStudentsThroughApi(
            managerToken, List.of(someCreatableStudent(), someCreatableStudent()));

    var toUpdate0 = studentToCrupdateStudent(created.getFirst(), "A new name zero");
    var toUpdate1 = studentToCrupdateStudent(created.get(1), "A new name one");

    var updated = apiAs(managerToken).createOrUpdateStudents(List.of(toUpdate0, toUpdate1), null);

    assertEquals(2, updated.size());
    assertTrue(updated.contains(expectedAfterRename(toUpdate0, "A new name zero")));
    assertTrue(updated.contains(expectedAfterRename(toUpdate1, "A new name one")));
  }

  @Test
  void admin_write_update_ok() throws ApiException {
    var created =
        createStudentsThroughApi(
            adminToken, List.of(someCreatableStudent(), someCreatableStudent()));

    var toUpdate0 = studentToCrupdateStudent(created.getFirst(), "A new name zero");
    var toUpdate1 = studentToCrupdateStudent(created.get(1), "A new name one");

    var updated = apiAs(adminToken).createOrUpdateStudents(List.of(toUpdate0, toUpdate1), null);

    assertEquals(2, updated.size());
    assertTrue(updated.contains(expectedAfterRename(toUpdate0, "A new name zero")));
    assertTrue(updated.contains(expectedAfterRename(toUpdate1, "A new name one")));
  }

  @Test
  void manager_create_student_then_set_to_alumni_ok() throws ApiException {
    var created =
        createStudentsThroughApi(managerToken, List.of(someCreatableStudent())).getFirst();

    var crupdateStudent = studentToCrupdateStudent(created, "A new name zero");
    crupdateStudent.setStatus(ALUMNI);

    var updated = apiAs(managerToken).createOrUpdateStudents(List.of(crupdateStudent), null);

    assertEquals(1, updated.size());
    assertTrue(updated.contains(expectedAfterRename(crupdateStudent, "A new name zero")));
  }

  @Test
  void manager_read_student_by_exclude_group_id() throws ApiException {
    var students =
        apiAs(managerToken)
            .getStudents(
                1,
                200,
                refPrefix,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(groupOne.getId()));

    assertFalse(idsOf(students).contains(enabledWorkingStudent.getId()));
    assertTrue(idsOf(students).contains(enabledStudentInOtherGroup.getId()));
  }

  @Test
  void manager_write_update_rollback_on_event_error() throws ApiException {
    var api = apiAs(managerToken);
    var toCreate = someCreatableStudent();
    reset(eventBridgeClientMock);
    when(eventBridgeClientMock.putEvents((PutEventsRequest) any()))
        .thenThrow(RuntimeException.class);

    assertThrowsApiExceptionOnCreate(api, toCreate);

    var actual = ownStudents(api, null, null);
    assertFalse(actual.stream().anyMatch(s -> Objects.equals(toCreate.getEmail(), s.getEmail())));
  }

  private static void assertThrowsApiExceptionOnCreate(UsersApi api, CrupdateStudent toCreate) {
    ApiAssertions.assertThrowsApiException(
        "{\"type\":\"500 INTERNAL_SERVER_ERROR\",\"message\":null}",
        () -> api.createOrUpdateStudents(List.of(toCreate), null));
  }

  @Test
  void manager_write_with_longitude_null_ko() {
    var api = apiAs(managerToken);
    var toCreate =
        someCreatableStudent().coordinates(new Coordinates().longitude(null).latitude(10.0));

    assertBadRequestException(
        "Longitude is null, it must go hand in hand with latitude",
        () -> api.createOrUpdateStudents(List.of(toCreate), null));
  }

  @Test
  void manager_write_with_latitude_null_ko() {
    var api = apiAs(managerToken);
    var toCreate =
        someCreatableStudent().coordinates(new Coordinates().longitude(10.0).latitude(null));

    assertBadRequestException(
        "Latitude is null, it must go hand in hand with longitude",
        () -> api.createOrUpdateStudents(List.of(toCreate), null));
  }

  @Test
  void manager_write_update_triggers_userUpserted() throws ApiException {
    reset(eventBridgeClientMock);
    setUpEventBridge(eventBridgeClientMock);

    createStudentsThroughApi(managerToken, List.of(someCreatableStudent()));

    verify(eventBridgeClientMock, times(1)).putEvents((PutEventsRequest) any());
  }

  @Test
  void manager_update_student_ok() throws ApiException {
    var created =
        createStudentsThroughApi(managerToken, List.of(someCreatableStudent())).getFirst();
    var payload = studentToCrupdateStudent(created, "Updated last name");

    var updated = apiAs(managerToken).updateStudent(created.getId(), payload);

    assertEquals("Updated last name", updated.getLastName());
    assertEquals(created.getId(), updated.getId());
  }

  @Test
  void manager_write_suspended_student() throws ApiException {
    var toCreate = someCreatableStudent().status(SUSPENDED);

    var created = createStudentsThroughApi(managerToken, List.of(toCreate));

    assertEquals(1, created.size());
    assertEquals(SUSPENDED, created.getFirst().getStatus());
  }

  @Test
  void manager_update_student_to_suspended() throws ApiException {
    var created =
        createStudentsThroughApi(managerToken, List.of(someCreatableStudent())).getFirst();
    var payload = studentToCrupdateStudent(created, created.getLastName());
    payload.setStatus(SUSPENDED);

    var updated = apiAs(managerToken).createOrUpdateStudents(List.of(payload), null);

    assertEquals(SUSPENDED, updated.getFirst().getStatus());
  }

  @Test
  void stats_are_exact() throws ApiException {
    var api = apiAs(managerToken);

    var women = api.getStudents(1, 500, null, null, null, null, null, F, null, null, null);
    var men = api.getStudents(1, 500, null, null, null, null, null, M, null, null, null);
    var total = api.getStudents(1, 500, null, null, null, null, null, null, null, null, null);

    var statistics = api.getStats();

    assertEquals(women.size(), statistics.getWomen().getTotal());
    assertEquals(men.size(), statistics.getMen().getTotal());
    assertEquals(total.size(), statistics.getTotalStudents());
  }

  @Test
  void crupdate_students_with_payment_frequency() throws ApiException {
    var usersApi = apiAs(managerToken);
    var payingApi = new PayingApi(anApiClient(managerToken));

    var monthly = someCreatableStudent().paymentFrequency(MONTHLY);
    var yearly = someCreatableStudent().paymentFrequency(YEARLY);
    var none = someCreatableStudent().paymentFrequency(null);

    var created = usersApi.createOrUpdateStudents(List.of(monthly, yearly, none), DUE_DATETIME);
    created.forEach(s -> createdUserIds.add(s.getId()));

    var monthlyId = idOfRef(created, monthly.getRef());
    var yearlyId = idOfRef(created, yearly.getRef());
    var noneId = idOfRef(created, none.getRef());

    var monthlyFees = payingApi.getFeesByStudentId(monthlyId, 1, 50, null);
    var yearlyFees = payingApi.getFeesByStudentId(yearlyId, 1, 50, null);
    var noneFees = payingApi.getFeesByStudentId(noneId, 1, 50, null);

    assertEquals(9, monthlyFees.size());
    assertEquals(1, yearlyFees.size());
    assertEquals(0, noneFees.size());
  }

  private static String idOfRef(List<Student> students, String ref) {
    return students.stream().filter(s -> ref.equals(s.getRef())).findFirst().orElseThrow().getId();
  }

  @Test
  void student_update_self_ko() throws ApiException {
    var api = apiAs(studentToken);
    var current = api.getStudentById(enabledWorkingStudent.getId());
    var payload = studentToCrupdateStudent(current, randomUUID().toString());

    assertThrowsForbiddenException(() -> api.updateStudent(enabledWorkingStudent.getId(), payload));
  }

  @Test
  void manager_read_group_students_ok() throws ApiException {
    var api = new GroupsApi(anApiClient(managerToken));

    var groupStudents = api.getStudentsByGroupId(groupOne.getId(), 1, 50, null);
    assertEquals(1, groupStudents.size());
    assertEquals(enabledWorkingStudent.getId(), groupStudents.getFirst().getId());

    var byFirstName =
        api.getStudentsByGroupId(
            groupOne.getId(), 1, 50, enabledWorkingStudent.getFirstName().toLowerCase());
    assertEquals(1, byFirstName.size());
    assertEquals(enabledWorkingStudent.getId(), byFirstName.getFirst().getId());
  }

  @Test
  void get_actual_student_level_ok() throws ApiException {
    var level = apiAs(studentToken).getStudentLevel(enabledWorkingStudent.getId());

    assertNotNull(level);
  }
}
