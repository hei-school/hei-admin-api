package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.SUSPENDED;
import static school.hei.haapi.endpoint.rest.model.PaymentFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.PaymentFrequency.YEARLY;
import static school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum.WORKER_STUDENT;
import static school.hei.haapi.endpoint.rest.model.Sex.F;
import static school.hei.haapi.endpoint.rest.model.Sex.M;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.COMMON_CORE;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.EL;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.NOT_WORKING;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.WORKING;
import static school.hei.haapi.integration.GroupIT.updatedGroup3;
import static school.hei.haapi.integration.GroupIT.updatedGroup5;
import static school.hei.haapi.integration.conf.TestUtils.EVENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.GROUP1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.PROMOTION1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT3_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT8_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT8_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.coordinatesWithNullValues;
import static school.hei.haapi.integration.conf.TestUtils.coordinatesWithValues;
import static school.hei.haapi.integration.conf.TestUtils.getMockedFile;
import static school.hei.haapi.integration.conf.TestUtils.group1;
import static school.hei.haapi.integration.conf.TestUtils.group2;
import static school.hei.haapi.integration.conf.TestUtils.group3;
import static school.hei.haapi.integration.conf.TestUtils.requestFile;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.conf.TestUtils.uploadProfilePicture;
import static school.hei.haapi.model.User.Role.MANAGER;
import static school.hei.haapi.model.User.Role.STUDENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.CrupdateStudent;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.model.Statistics;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.service.UserService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

@Testcontainers
@AutoConfigureMockMvc
public class StudentIT extends FacadeITMockedThirdParties {
  public static final Instant DUE_DATETIME = Instant.parse("2021-11-08T08:25:24.00Z");
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @Autowired ObjectMapper objectMapper;
  @Autowired UserService userService;
  @Autowired UserMapper userMapper;
  @Autowired TestUtils testUtils;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  public static Student student1() {
    Student student = new Student();
    student.setId("student1_id");
    student.setFirstName("Ryan");
    student.setLastName("Andria");
    student.setEmail("test+ryan@hei.school");
    student.setRef("STD21001");
    student.setPhone("0322411123");
    student.setStatus(ENABLED);
    student.setSex(M);
    student.setBirthDate(LocalDate.parse("2000-01-01"));
    student.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    student.setAddress("Adr 1");
    student.setNic("");
    student.setSpecializationField(COMMON_CORE);
    student.setBirthPlace("");
    student.setCoordinates(new Coordinates().longitude(-123.123).latitude(123.0));
    student.setHighSchoolOrigin("Lycée Andohalo");
    student.setWorkStudyStatus(WORKING);
    student.setProfessionalExperience(WORKER_STUDENT);
    student.setCommitmentBeginDate(Instant.parse("2021-11-08T08:25:24Z"));
    student.setGroups(List.of(group1(), group2()));
    student.setIsRepeatingYear(false);
    return student;
  }

  public static Student student2() {
    Student student = new Student();
    student.setId("student2_id");
    student.setFirstName("Two");
    student.setLastName("Student");
    student.setEmail("test+student2@hei.school");
    student.setRef("STD21002");
    student.setPhone("0322411124");
    student.setStatus(ENABLED);
    student.setSex(F);
    student.setBirthDate(LocalDate.parse("2000-01-02"));
    student.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student.setAddress("Adr 2");
    student.setBirthPlace("");
    student.setNic("");
    student.setSpecializationField(COMMON_CORE);
    student.setCoordinates(new Coordinates().longitude(255.255).latitude(-255.255));
    student.setHighSchoolOrigin("Lycée Andohalo");
    student.setWorkStudyStatus(WORKING);
    student.setProfessionalExperience(WORKER_STUDENT);
    student.setCommitmentBeginDate(Instant.parse("2021-11-08T08:25:24.00Z"));
    student.setGroups(List.of(group1()));
    student.setIsRepeatingYear(false);
    return student;
  }

  public static CrupdateStudent createStudent2() {
    CrupdateStudent student = new CrupdateStudent();
    student.setId("student2_id");
    student.setFirstName("Two");
    student.setLastName("Student");
    student.setEmail("test+student2@hei.school");
    student.setRef("STD21002");
    student.setPhone("0322411124");
    student.setStatus(ENABLED);
    student.setSex(F);
    student.setBirthDate(LocalDate.parse("2000-01-02"));
    student.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student.setAddress("Adr 2");
    student.setBirthPlace("");
    student.setNic("");
    student.setCoordinates(coordinatesWithNullValues());

    return student;
  }

  public static Student student3() {
    Student student = new Student();
    student.setId("student3_id");
    student.setFirstName("Three");
    student.setLastName("Student");
    student.setEmail("test+student3@hei.school");
    student.setRef("STD21003");
    student.setPhone("0322411124");
    student.setStatus(ENABLED);
    student.setSex(F);
    student.setBirthDate(LocalDate.parse("2000-01-02"));
    student.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student.setAddress("Adr 2");
    student.setBirthPlace("Befelatanana");
    student.setNic("0000000000");
    student.setSpecializationField(COMMON_CORE);
    student.setCoordinates(coordinatesWithNullValues());
    student.setHighSchoolOrigin("Lycée Analamahitsy");
    student.setWorkStudyStatus(NOT_WORKING);
    student.setGroups(List.of());
    student.setIsRepeatingYear(false);
    return student;
  }

  public static Student disabledStudent1() {
    return new Student()
        .id("student4_id")
        .firstName("Disable")
        .lastName("One")
        .email("test+disable1@hei.school")
        .ref("STD29001")
        .status(EnableStatus.DISABLED)
        .sex(M)
        .birthDate(LocalDate.parse("2000-12-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .phone("0322411123")
        .specializationField(COMMON_CORE)
        .nic("")
        .birthPlace("")
        .coordinates(coordinatesWithNullValues())
        .workStudyStatus(NOT_WORKING)
        .address("Adr 1")
        .groups(List.of())
        .isRepeatingYear(false);
  }

  public static CrupdateStudent creatableSuspendedStudent() {
    return new CrupdateStudent()
        .firstName("Suspended")
        .lastName("Two")
        .email("test+suspended2@hei.school")
        .ref("STD29004")
        .status(SUSPENDED)
        .sex(F)
        .birthDate(LocalDate.parse("2000-12-02"))
        .entranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"))
        .phone("0322411124")
        .address("Adr 3")
        .coordinates(coordinatesWithNullValues());
  }

  public static Student suspendedStudent1() {
    return new Student()
        .id("student6_id")
        .firstName("Suspended")
        .lastName("One")
        .email("test+suspended@hei.school")
        .ref("STD29003")
        .status(SUSPENDED)
        .sex(F)
        .birthDate(LocalDate.parse("2000-12-02"))
        .entranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"))
        .phone("0322411124")
        .nic("")
        .specializationField(COMMON_CORE)
        .birthPlace("")
        .address("Adr 2")
        .workStudyStatus(NOT_WORKING)
        .coordinates(coordinatesWithNullValues())
        .groups(List.of())
        .isRepeatingYear(false);
  }

  public static Student repeatingStudent1() {
    Group copyGroup3 = new Group();
    copyGroup3.setId(group3().getId());
    copyGroup3.setRef(group3().getRef());
    copyGroup3.setCreationDatetime(group3().getCreationDatetime());
    copyGroup3.setName(group3().getName());
    copyGroup3.setSize(1);
    return new Student()
        .id("student7_id")
        .firstName("Repeating")
        .lastName("One")
        .email("test+repeating1@hei.school")
        .ref("STD22090")
        .status(ENABLED)
        .sex(M)
        .birthDate(LocalDate.parse("2000-12-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .phone("0322411190")
        .nic("")
        .specializationField(COMMON_CORE)
        .birthPlace("")
        .address("Adr 1")
        .workStudyStatus(NOT_WORKING)
        .coordinates(coordinatesWithNullValues())
        .groups(List.of(updatedGroup3()))
        .isRepeatingYear(Boolean.TRUE);
  }

  public static Student repeatingStudent2() {
    return new Student()
        .id("student8_id")
        .firstName("Repeating")
        .lastName("Two")
        .email("test+repeating2@hei.school")
        .ref("STD23090")
        .status(ENABLED)
        .sex(F)
        .birthDate(LocalDate.parse("2000-12-02"))
        .entranceDatetime(Instant.parse("2022-11-09T08:26:24.00Z"))
        .phone("0322411191")
        .nic("")
        .specializationField(COMMON_CORE)
        .birthPlace("")
        .address("Adr 2")
        .workStudyStatus(NOT_WORKING)
        .coordinates(coordinatesWithNullValues())
        .groups(List.of(updatedGroup5()))
        .isRepeatingYear(Boolean.TRUE);
  }

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void manager_generate_group_students_ok() throws IOException, InterruptedException {
    var randomManager = testUtils.createSomeUser(MANAGER, User.Status.ENABLED);
    userService.saveAll(List.of(randomManager));
    String STUDENTS_GROUP = "/groups/" + GROUP1_ID + "/students/raw";
    HttpClient httpClient = HttpClient.newBuilder().build();
    String basePath = "http://localhost:" + localPort;

    HttpResponse<byte[]> response =
        httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(basePath + STUDENTS_GROUP))
                .GET()
                .header("Authorization", "Bearer " + randomManager.getEmail())
                .build(),
            HttpResponse.BodyHandlers.ofByteArray());

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
    assertNotNull(response);
  }

  @Test
  void manager_generate_event_participants_ok() throws IOException, InterruptedException {
    var randomManager = testUtils.createSomeUser(MANAGER, User.Status.ENABLED);
    userService.saveAll(List.of(randomManager));
    String basePath = "http://localhost:" + localPort;
    var response =
        testUtils.requestFile(
            URI.create(basePath + "/event/" + EVENT1_ID + "/students/raw/xlsx"),
            randomManager.getEmail());
    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
    assertNotNull(response);
  }

  @Test
  void manager_generate_student_in_promotion_ok() throws IOException, InterruptedException {
    String basePath = "http://localhost:" + localPort;
    var response =
        requestFile(
            URI.create(basePath + "/promotion/" + PROMOTION1_ID + "/students/raw/xlsx"),
            MANAGER1_TOKEN);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
    assertNotNull(response);
  }

  @Test
  void manager_generate_all_student_ok() throws IOException, InterruptedException {
    String basePath = "http://localhost:" + localPort;
    var response = requestFile(URI.create(basePath + "/students/raw/xlsx"), MANAGER1_TOKEN);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
    assertNotNull(response);
  }

  @Test
  void student_read_itself_repeating_this_year_ok() throws ApiException {
    ApiClient student8Client = anApiClient(STUDENT8_TOKEN);

    UsersApi api = new UsersApi(student8Client);
    Student actual = api.getStudentById(STUDENT8_ID);

    assertEquals(repeatingStudent2(), actual);
    assertEquals(Boolean.TRUE, actual.getIsRepeatingYear());
  }

  @Test
  @Disabled("TODO")
  void manager_read_repeating_student_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(1, 10, null, null, null, null, null, null, null, null, null);

    assertTrue(actualStudents.containsAll(List.of(repeatingStudent2(), repeatingStudent1())));
  }

  @Test
  void manager_upload_profile_picture() throws IOException, InterruptedException {

    HttpResponse<InputStream> response =
        uploadProfilePicture(localPort, MANAGER1_TOKEN, STUDENT1_ID, "students");

    Student student = objectMapper.readValue(response.body(), Student.class);

    assertEquals(200, response.statusCode());
    assertEquals("STD21001", student.getRef());
  }

  @Test
  @Disabled
  // TODO: Same here
  void student_update_other_profile_picture_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);
    assertThrowsForbiddenException(
        () -> api.uploadStudentProfilePicture(STUDENT3_ID, getMockedFile("img", ".png")));
  }

  @Test
  @Disabled
  // TODO: Check why this returns null while a Forbidden Exception is thrown
  void student_update_own_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);
    assertThrowsForbiddenException(
        () -> api.uploadStudentProfilePicture(STUDENT1_ID, getMockedFile("img", ".png")));
  }

  @Test
  @Disabled("TODO")
  void student_read_own_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    UsersApi api = new UsersApi(student1Client);
    Student actual = api.getStudentById(STUDENT1_ID);

    assertEquals(student1(), actual);
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);

    assertThrowsForbiddenException(() -> api.getStudentById(TestUtils.STUDENT2_ID));

    assertThrowsForbiddenException(
        () -> api.getStudents(1, 20, null, null, null, null, null, null, null, null, null));
  }

  @Test
  @Disabled("TODO")
  void teacher_read_ok() throws ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    UsersApi api = new UsersApi(teacher1Client);
    Student actualStudent1 = api.getStudentById(STUDENT1_ID);

    List<Student> actualStudents =
        api.getStudents(1, 20, null, null, null, null, null, null, null, null, null);

    assertEquals(student1(), actualStudent1);
    assertTrue(actualStudents.contains(student1()));
    assertTrue(actualStudents.contains(student2()));
  }

  @Test
  void manager_read_by_disabled_status_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(
            1, 10, null, null, null, null, EnableStatus.DISABLED, null, null, null, null);
    assertEquals(2, actualStudents.size());
    assertTrue(actualStudents.contains(disabledStudent1()));
  }

  @Test
  @Disabled("TODO")
  void manager_read_by_suspended_status_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(1, 10, null, null, null, null, SUSPENDED, null, null, null, null);
    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(suspendedStudent1()));
  }

  @Test
  @Disabled("TODO")
  void manager_read_by_work_status_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(1, 10, null, null, null, null, null, null, WORKING, null, null);

    assertEquals(2, actualStudents.size());
    assertTrue(actualStudents.containsAll(List.of(student2(), student1())));
  }

  @Test
  void manager_read_by_status_and_sex_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(1, 10, null, null, null, null, EnableStatus.DISABLED, F, null, null, null);
    assertEquals(1, actualStudents.size());
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);

    assertThrowsForbiddenException(() -> api.createOrUpdateStudents(List.of(), null));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    UsersApi api = new UsersApi(teacher1Client);

    assertThrowsForbiddenException(() -> api.createOrUpdateStudents(List.of(), null));
  }

  @Test
  @Disabled("TODO")
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(1, 20, null, null, null, null, null, null, null, null, null);

    assertTrue(actualStudents.contains(student1()));
    assertTrue(actualStudents.contains(student2()));
    assertTrue(actualStudents.contains(student3()));
  }

  @Test
  void manager_read_displayed_commitment_date() throws ApiException, InterruptedException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(
            1,
            20,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Instant.parse("2021-11-08T08:25:24Z"),
            null);

    assertEquals(2, actualStudents.size());
    assertEquals(
        student1().getCommitmentBeginDate(), actualStudents.getFirst().getCommitmentBeginDate());
  }

  @Test
  @Disabled("TODO")
  void manager_read_by_ref_and_name_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(
            1,
            20,
            student1().getRef(),
            student1().getFirstName(),
            student1().getLastName(),
            null,
            null,
            null,
            null,
            null,
            null);

    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(student1()));
  }

  @Test
  @Disabled("TODO")
  void manager_read_by_ref_ignoring_case_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(1, 20, "std21001", null, null, null, null, null, null, null, null);

    assertEquals("STD21001", student1().getRef());
    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(student1()));
  }

  @Test
  @Disabled("TODO")
  void manager_read_by_ref_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(1, 20, student1().getRef(), null, null, null, null, null, null, null, null);

    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(student1()));
  }

  @Test
  @Disabled("")
  void manager_read_by_last_name_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(
            1, 20, null, null, student2().getLastName(), null, null, null, null, null, null);

    assertEquals(2, actualStudents.size());
    assertTrue(actualStudents.contains(student2()));
    assertTrue(actualStudents.contains(student3()));
  }

  @Test
  @Disabled("TODO")
  void manager_read_by_ref_and_last_name_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(
            1,
            20,
            student2().getRef(),
            null,
            student2().getLastName(),
            null,
            null,
            null,
            null,
            null,
            null);

    assertEquals(1, actualStudents.size());
    assertTrue(actualStudents.contains(student2()));
  }

  @Test
  void manager_read_by_ref_and_bad_name_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actualStudents =
        api.getStudents(
            1,
            20,
            student2().getRef(),
            null,
            student1().getLastName(),
            null,
            null,
            null,
            null,
            null,
            null);

    assertEquals(0, actualStudents.size());
    assertFalse(actualStudents.contains(student1()));
  }

  @Test
  @Disabled("TODO")
  void monitor_read_students_ok() throws ApiException {
    ApiClient monitorClient = anApiClient(MONITOR1_TOKEN);
    UsersApi api = new UsersApi(monitorClient);
    List<Student> actual =
        api.getStudents(1, 20, null, null, null, null, null, null, null, null, null);

    assertTrue(actual.contains(student1()));
    assertTrue(actual.contains(student2()));
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    var randomManager = testUtils.createSomeUser(MANAGER, User.Status.ENABLED);
    userService.saveAll(List.of(randomManager));
    ApiClient client = anApiClient(randomManager.getEmail());
    UsersApi api = new UsersApi(client);

    User user1 = testUtils.createSomeUser(STUDENT, User.Status.ENABLED);
    User user2 = testUtils.createSomeUser(STUDENT, User.Status.ENABLED);
    userService.saveAll(List.of(user1, user2));

    Student student1 = userMapper.toRestStudent(user1);
    Student student2 = userMapper.toRestStudent(user2);

    CrupdateStudent crupdateStudent1 = toCrupdateStudent(student1).firstName("Test");
    CrupdateStudent crupdateStudent2 = toCrupdateStudent(student2).address("Home");
    List<Student> updated =
        api.createOrUpdateStudents(List.of(crupdateStudent1, crupdateStudent2), null);

    Student updated1 = student1.firstName("Test");
    Student updated2 = student2.address("Home");

    assertEquals(2, updated.size());
    assertTrue(updated.contains(updated1));
    assertTrue(updated.contains(updated2));
  }

  @Test
  void manager_read_student_by_exclude_group_id() throws ApiException {
    var randomManager = testUtils.createSomeUser(MANAGER, User.Status.ENABLED);
    ApiClient manager1Client = anApiClient(randomManager.getEmail());
    UsersApi api = new UsersApi(manager1Client);

    List<Student> students =
        api.getStudents(
            1, 10, null, null, null, null, null, null, null, null, List.of("group1_id"));

    assertTrue(students.contains(student3()));
    assertFalse(students.contains(student1()));
    assertFalse(students.contains(student2()));
  }

  @Test
  void manager_write_update_rollback_on_event_error() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    CrupdateStudent toCreate =
        toCrupdateStudent(
            userMapper.toRestStudent(testUtils.createSomeUser(STUDENT, User.Status.ENABLED)));
    reset(eventBridgeClientMock);
    when(eventBridgeClientMock.putEvents((PutEventsRequest) any()))
        .thenThrow(RuntimeException.class);

    assertThrowsApiException(
        "{\"type\":\"500 INTERNAL_SERVER_ERROR\",\"message\":null}",
        () -> api.createOrUpdateStudents(List.of(toCreate), null));

    List<Student> actual =
        api.getStudents(1, 100, null, null, null, null, null, null, null, null, null);
    assertFalse(actual.stream().anyMatch(s -> Objects.equals(toCreate.getEmail(), s.getEmail())));
  }

  @Test
  @Disabled("TODO: poja has removed max put entries check")
  void manager_write_update_more_than_10_students_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    CrupdateStudent studentToCreate =
        toCrupdateStudent(
            userMapper.toRestStudent(testUtils.createSomeUser(STUDENT, User.Status.ENABLED)));
    List<CrupdateStudent> listToCreate =
        testUtils.createSomeUsers(11, STUDENT, User.Status.ENABLED).stream()
            .map(user -> toCrupdateStudent(userMapper.toRestStudent(user)))
            .toList();
    listToCreate.add(studentToCreate);

    assertThrowsApiException(
        "{\"type\":\"500 INTERNAL_SERVER_ERROR\",\"message\":\"Request entries must be <= 10\"}",
        () -> api.createOrUpdateStudents(listToCreate, null));

    List<Student> actual =
        api.getStudents(1, 100, null, null, null, null, null, null, null, null, null);
    assertFalse(
        actual.stream().anyMatch(s -> Objects.equals(studentToCreate.getEmail(), s.getEmail())));
  }

  @Test
  void manager_write_with_coordinates_null_ko() throws ApiException {
    var randomManager = testUtils.createSomeUser(MANAGER, User.Status.ENABLED);
    userService.saveAll(List.of(randomManager));
    ApiClient manager1Client = anApiClient(randomManager.getEmail());
    UsersApi api = new UsersApi(manager1Client);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Longitude is null, it must go hand in hand"
            + " with latitude\"}",
        () ->
            api.createOrUpdateStudents(
                List.of(
                    toCrupdateStudent(
                            userMapper.toRestStudent(
                                testUtils.createSomeUser(STUDENT, User.Status.ENABLED)))
                        .coordinates(new Coordinates().longitude(null).latitude(null))),
                null));
  }

  @Test
  void manager_write_update_triggers_userUpserted() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    reset(eventBridgeClientMock);
    when(eventBridgeClientMock.putEvents((PutEventsRequest) any()))
        .thenReturn(
            PutEventsResponse.builder()
                .entries(
                    PutEventsResultEntry.builder().eventId("eventId1").build(),
                    PutEventsResultEntry.builder().eventId("eventId2").build())
                .build());

    List<CrupdateStudent> crupdateStudents =
        testUtils.createSomeUsers(2, STUDENT, User.Status.ENABLED).stream()
            .map(user -> toCrupdateStudent(userMapper.toRestStudent(user)))
            .toList();
    List<Student> created = api.createOrUpdateStudents(crupdateStudents, null);

    ArgumentCaptor<PutEventsRequest> captor = ArgumentCaptor.forClass(PutEventsRequest.class);
    verify(eventBridgeClientMock, times(1)).putEvents(captor.capture());
    PutEventsRequest actualRequest = captor.getValue();
    List<PutEventsRequestEntry> actualRequestEntries = actualRequest.entries();
    assertEquals(2, actualRequestEntries.size());
    Student created0 = created.getFirst();
    PutEventsRequestEntry requestEntry0 = actualRequestEntries.getFirst();
    assertTrue(requestEntry0.detail().contains(created0.getId()));
    assertTrue(requestEntry0.detail().contains(created0.getEmail()));
    Student created1 = created.get(1);
    PutEventsRequestEntry requestEntry1 = actualRequestEntries.get(1);
    assertTrue(requestEntry1.detail().contains(created1.getId()));
    assertTrue(requestEntry1.detail().contains(created1.getEmail()));
  }

  @Test
  @Disabled("dirty")
  void manager_update_student_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    CrupdateStudent student2ToUpdate = createStudent2();
    student2ToUpdate.setAddress("updated address");
    student2ToUpdate.setNic("updated nic");
    student2ToUpdate.setBirthPlace("updated birthplace");
    student2ToUpdate.setCoordinates(coordinatesWithValues());
    student2ToUpdate.setSpecializationField(EL);
    student2ToUpdate.setHighSchoolOrigin("Lycée Saint Gabriel Mahajanga");

    Student updatedStudent2 = student2();
    updatedStudent2.setBirthPlace("updated birthplace");
    updatedStudent2.setNic("updated nic");
    updatedStudent2.setSpecializationField(EL);
    updatedStudent2.setAddress("updated address");
    updatedStudent2.setCoordinates(coordinatesWithValues());
    updatedStudent2.setHighSchoolOrigin("Lycée Saint Gabriel Mahajanga");

    Student actualUpdated = api.updateStudent(STUDENT2_ID, student2ToUpdate);

    assertEquals(updatedStudent2, actualUpdated);
  }

  @Test
  @Disabled("TODO")
  void manager_read_students_by_commitment_begin() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actual =
        api.getStudents(
            1,
            10,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Instant.parse("2021-11-08T08:25:24.00Z"),
            null);

    assertEquals(2, actual.size());
    assertTrue(actual.containsAll(List.of(student1(), student2())));
  }

  @Test
  @Disabled("dirty")
  void manager_write_suspended_student() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actual = api.createOrUpdateStudents(List.of(creatableSuspendedStudent()), null);
    Student created = actual.get(0);
    List<Student> suspended =
        api.getStudents(1, 10, null, "Suspended", null, null, SUSPENDED, null, null, null, null);

    assertTrue(suspended.contains(created));
    assertEquals(1, actual.size());
  }

  @Test
  @Disabled("dirty")
  void manager_update_student_to_suspended() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actual =
        api.createOrUpdateStudents(List.of(createStudent2().status(SUSPENDED)), null);
    Student updated = actual.getFirst();
    List<Student> suspended =
        api.getStudents(1, 10, null, null, null, null, SUSPENDED, null, null, null, null);

    assertTrue(suspended.contains(updated));
    assertEquals(1, actual.size());
  }

  @Test
  void stats_are_exact() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi usersApi = new UsersApi(manager1Client);

    Integer women =
        usersApi.getStudents(1, 200, null, null, null, null, null, F, null, null, null).size();
    Integer men =
        usersApi.getStudents(1, 200, null, null, null, null, null, M, null, null, null).size();
    Integer totalStudents =
        usersApi.getStudents(1, 200, null, null, null, null, null, null, null, null, null).size();

    Statistics statistics = usersApi.getStats();
    assertEquals(statistics.getWomen().getTotal(), women);
    assertEquals(statistics.getMen().getTotal(), men);
    assertEquals(statistics.getTotalStudents(), totalStudents);
  }

  @Test
  void crupdate_students_with_payment_frequency() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    UsersApi usersApi = new UsersApi(apiClient);
    PayingApi payingApi = new PayingApi(apiClient);

    CrupdateStudent creatableStudent1 =
        toCrupdateStudent(
            userMapper.toRestStudent(testUtils.createSomeUser(STUDENT, User.Status.ENABLED)));
    creatableStudent1.setPaymentFrequency(MONTHLY);

    CrupdateStudent creatableStudent2 =
        toCrupdateStudent(
            userMapper.toRestStudent(testUtils.createSomeUser(STUDENT, User.Status.ENABLED)));
    creatableStudent2.setPaymentFrequency(YEARLY);

    CrupdateStudent creatableStudent3 =
        toCrupdateStudent(
            userMapper.toRestStudent(testUtils.createSomeUser(STUDENT, User.Status.ENABLED)));
    creatableStudent3.setPaymentFrequency(null);

    List<Student> studentsCreated =
        usersApi.createOrUpdateStudents(
            List.of(creatableStudent1, creatableStudent2, creatableStudent3), DUE_DATETIME);

    Student student1 =
        usersApi
            .getStudents(
                1, 15, creatableStudent1.getRef(), null, null, null, null, null, null, null, null)
            .getFirst();
    Student student2 =
        usersApi
            .getStudents(
                1, 15, creatableStudent2.getRef(), null, null, null, null, null, null, null, null)
            .getFirst();
    Student student3 =
        usersApi
            .getStudents(
                1, 15, creatableStudent3.getRef(), null, null, null, null, null, null, null, null)
            .getFirst();

    assertTrue(studentsCreated.containsAll(List.of(student1, student2, student3)));

    List<Fee> student1Fees = payingApi.getStudentFees(student1.getId(), 1, 15, null);
    List<Fee> student2Fees = payingApi.getStudentFees(student2.getId(), 1, 15, null);
    List<Fee> student3Fees = payingApi.getStudentFees(student3.getId(), 1, 15, null);

    // Verify size
    assertEquals(9, student1Fees.size());
    assertEquals(1, student2Fees.size());
    assertEquals(0, student3Fees.size());
  }

  @Test
  void student_update_self_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);
    Student currentStudent1 = api.getStudentById(STUDENT1_ID);
    Student expectedStudent1AfterUpdate = randomizeStudentUpdatableValues(currentStudent1);
    CrupdateStudent payload = toCrupdateStudent(expectedStudent1AfterUpdate);

    assertThrowsForbiddenException(() -> api.updateStudent(STUDENT1_ID, payload));
  }

  @Test
  @Disabled("TODO: check group1 students")
  void manager_read_group_students_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Student> actualGroupStudents = api.getStudentsByGroupId(GROUP1_ID, 1, 10, null);
    assertEquals(2, actualGroupStudents.size());

    List<Student> actualGroupStudentsByRef = api.getStudentsByGroupId(GROUP1_ID, 1, 10, "Ryan");
    assertEquals(1, actualGroupStudentsByRef.size());
    assertEquals(student1(), actualGroupStudentsByRef.getFirst());

    List<Student> actualGroupStudentsByFirstName =
        api.getStudentsByGroupId(GROUP1_ID, 1, 10, "ryan");
    assertEquals(1, actualGroupStudentsByFirstName.size());
    assertEquals(student1(), actualGroupStudentsByFirstName.getFirst());
  }

  private CrupdateStudent toCrupdateStudent(Student student) {
    return new CrupdateStudent()
        .id(student.getId())
        .birthDate(student.getBirthDate())
        .id(student.getId())
        .entranceDatetime(student.getEntranceDatetime())
        .phone(student.getPhone())
        .nic(student.getNic())
        .birthPlace(student.getBirthPlace())
        .email(student.getEmail())
        .address(student.getAddress())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .sex(student.getSex())
        .ref(student.getRef())
        .specializationField(student.getSpecializationField())
        .status(student.getStatus());
  }

  private Student randomizeStudentUpdatableValues(Student student) {
    return new Student()
        .id(student.getId())
        .entranceDatetime(student.getEntranceDatetime())
        .status(student.getStatus())
        .email(student.getEmail())
        .ref(student.getRef())
        .birthDate(LocalDate.parse("2000-12-05"))
        .birthPlace(randomUUID().toString())
        .nic(randomUUID().toString())
        .phone(randomUUID().toString())
        .sex(student.getSex() != null ? (student.getSex().equals(M) ? F : M) : null)
        .address(randomUUID().toString())
        .lastName(randomUUID().toString())
        .firstName(randomUUID().toString())
        .specializationField(student.getSpecializationField());
  }
}
