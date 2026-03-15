package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.StatusCheckResult.PENDING;
import static school.hei.haapi.endpoint.rest.model.StatusCheckResult.WITHDRAWN;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT_AXEL_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anApiClient;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.getCasdoorUserAxel;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.freddy;
import static school.hei.haapi.integration.test_data.StudentTestData.manitra;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.test_data.TeacherTestData.toky;
import static school.hei.haapi.model.User.Status.ALUMNI;
import static school.hei.haapi.model.User.Status.DISABLED;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.api.StudentApi;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateStatusCheck;
import school.hei.haapi.endpoint.rest.model.StatusCheckResult;
import school.hei.haapi.endpoint.rest.model.UpdateStatusCheck;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.StatusCheck;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.StatusCheckRepository;
import school.hei.haapi.repository.UserRepository;

public class StatusCheckIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private StatusCheckRepository statusCheckRepository;
  private User teacherToky;
  private User enabledStudentAxel;
  private StatusCheck axelStatusCheck;
  private User enabledStudentTolojanahary;
  private StatusCheck tolojanaharyStatusCheck;
  private User disabledStudentFreddy;
  private User alumniStudentManitra;

  void setUpTestData() {
    teacherToky = userRepository.save(toky());
    enabledStudentAxel = userRepository.save(axel());
    enabledStudentTolojanahary = userRepository.save(tolojanahary());

    disabledStudentFreddy = freddy();
    disabledStudentFreddy.setStatus(DISABLED);
    disabledStudentFreddy = userRepository.save(disabledStudentFreddy);

    alumniStudentManitra = manitra();
    alumniStudentManitra.setStatus(ALUMNI);
    alumniStudentManitra = userRepository.save(alumniStudentManitra);

    axelStatusCheck = statusCheckRepository.save(aStatusCheck(enabledStudentAxel, teacherToky));
    tolojanaharyStatusCheck =
        statusCheckRepository.save(
            aStatusCheck(enabledStudentTolojanahary, teacherToky, WITHDRAWN));
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpTestData();
  }

  @AfterEach
  void tearDown() {
    statusCheckRepository.deleteAll();
    userRepository.deleteAll(
        List.of(
            teacherToky,
            enabledStudentAxel,
            enabledStudentTolojanahary,
            disabledStudentFreddy,
            alumniStudentManitra));
  }

  @Test
  void getStudentStatusChecks_byManagerOrAdminOrTeacher_ok() throws ApiException {
    var managerResponse = studentApiAsManager().getStudentStatusChecks(enabledStudentAxel.getId());
    var adminResponse = studentApiAsAdmin().getStudentStatusChecks(enabledStudentAxel.getId());
    var teacherResponse = studentApiAsTeacher().getStudentStatusChecks(enabledStudentAxel.getId());

    assertEquals(axelStatusCheck.getId(), managerResponse.getFirst().getId());
    assertEquals(axelStatusCheck.getId(), adminResponse.getFirst().getId());
    assertEquals(axelStatusCheck.getId(), teacherResponse.getFirst().getId());
  }

  @Test
  void getStudentStatusChecks_byConcernedStudent_ok() throws ApiException {
    enabledStudentAxel.setEmail(getCasdoorUserAxel().getEmail());
    userRepository.save(enabledStudentAxel);
    var response = studentApiAsAxel().getStudentStatusChecks(enabledStudentAxel.getId());

    assertEquals(axelStatusCheck.getId(), response.getFirst().getId());
    assertEquals(enabledStudentAxel.getId(), response.getFirst().getConcernedStudent().getId());
  }

  @Test
  void getStudentStatusChecks_byOtherStudent_ko() throws ApiException {
    StudentApi tolojanaharyApi = new StudentApi(anApiClient(STUDENT2_TOKEN, localPort));

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> tolojanaharyApi.getStudentStatusChecks(enabledStudentAxel.getId()));
  }

  @Test
  void createStudentStatusCheck_byManagerOrAdminOrTeacher_ok() throws ApiException {
    var managerCreated =
        studentApiAsManager()
            .createStatusChecks(
                enabledStudentTolojanahary.getId(),
                aCreateStatusCheck(enabledStudentTolojanahary, teacherToky));
    var adminCreated =
        studentApiAsAdmin()
            .createStatusChecks(
                enabledStudentTolojanahary.getId(),
                aCreateStatusCheck(enabledStudentTolojanahary, teacherToky));
    var teacherCreated =
        studentApiAsTeacher()
            .createStatusChecks(
                enabledStudentTolojanahary.getId(),
                aCreateStatusCheck(enabledStudentTolojanahary, teacherToky));

    assertNotNull(managerCreated.getId());
    assertNotNull(adminCreated.getId());
    assertNotNull(teacherCreated.getId());
    assertEquals(PENDING, managerCreated.getResult());
    assertEquals(enabledStudentTolojanahary.getId(), managerCreated.getConcernedStudent().getId());
  }

  @Test
  void createStudentStatusCheck_byStudent_ko() {
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            studentApiAsStudent1()
                .createStatusChecks(
                    enabledStudentAxel.getId(),
                    aCreateStatusCheck(enabledStudentAxel, teacherToky)));
  }

  @Test
  void createStudentStatusCheck_forDisabledStudent_ko() throws ApiException {
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Cannot create a status check: Student with ref"
            + " : "
            + disabledStudentFreddy.getRef()
            + " is already DISABLED or an ALUMNI\"}",
        () ->
            studentApiAsTeacher()
                .createStatusChecks(
                    disabledStudentFreddy.getId(),
                    aCreateStatusCheck(disabledStudentFreddy, teacherToky)));
  }

  @Test
  void createStudentStatusCheck_forAlumniStudent_ko() {
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Cannot create a status check: Student with ref"
            + " : "
            + alumniStudentManitra.getRef()
            + " is already DISABLED or an ALUMNI\"}",
        () ->
            studentApiAsTeacher()
                .createStatusChecks(
                    alumniStudentManitra.getId(),
                    aCreateStatusCheck(alumniStudentManitra, teacherToky)));
  }

  @Test
  void updateStudentStatusCheck_byManager_ok() throws ApiException {
    var updated =
        studentApiAsManager()
            .updateStatusCheck(
                enabledStudentAxel.getId(), axelStatusCheck.getId(), anUpdateStatusCheck());
    assertEquals("Updated content", updated.getDescription());
    assertEquals(WITHDRAWN, updated.getResult());
  }

  @Test
  void updateStudentStatusCheck_byAdmin_ok() throws ApiException {
    var updated =
        studentApiAsAdmin()
            .updateStatusCheck(
                enabledStudentAxel.getId(), axelStatusCheck.getId(), anUpdateStatusCheck());
    assertEquals("Updated content", updated.getDescription());
    assertEquals(WITHDRAWN, updated.getResult());
  }

  @Test
  void updateStudentStatusCheck_byTeacher_ok() throws ApiException {
    var updated =
        studentApiAsTeacher()
            .updateStatusCheck(
                enabledStudentAxel.getId(), axelStatusCheck.getId(), anUpdateStatusCheck());
    assertEquals("Updated content", updated.getDescription());
    assertEquals(WITHDRAWN, updated.getResult());
  }

  @Test
  void updateStudentStatusCheck_byStudent_ko() {
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () ->
            studentApiAsStudent1()
                .updateStatusCheck(
                    enabledStudentAxel.getId(), axelStatusCheck.getId(), anUpdateStatusCheck()));
  }

  @Test
  void getAllStatusChecks_defaultsPending_byManagerOrAdminOrTeacher_ok() throws ApiException {
    var managerResponse = studentApiAsManager().getAllStatusChecks(null);
    var adminResponse = studentApiAsAdmin().getAllStatusChecks(null);
    var teacherResponse = studentApiAsTeacher().getAllStatusChecks(null);

    assertTrue(managerResponse.stream().allMatch(sc -> PENDING.equals(sc.getResult())));
    assertTrue(adminResponse.stream().allMatch(sc -> PENDING.equals(sc.getResult())));
    assertTrue(teacherResponse.stream().allMatch(sc -> PENDING.equals(sc.getResult())));
    assertTrue(managerResponse.stream().anyMatch(sc -> sc.getId().equals(axelStatusCheck.getId())));
  }

  @Test
  void getAllStatusChecks_filteredByResult_ok() throws ApiException {
    var pendingStatusChecks = studentApiAsManager().getAllStatusChecks(PENDING);
    var withdrawnStatusChecks = studentApiAsManager().getAllStatusChecks(WITHDRAWN);

    assertTrue(pendingStatusChecks.stream().allMatch(sc -> PENDING.equals(sc.getResult())));
    assertTrue(
        pendingStatusChecks.stream().anyMatch(sc -> sc.getId().equals(axelStatusCheck.getId())));
    assertTrue(withdrawnStatusChecks.stream().allMatch(sc -> WITHDRAWN.equals(sc.getResult())));
    assertTrue(
        withdrawnStatusChecks.stream()
            .anyMatch(sc -> sc.getId().equals(tolojanaharyStatusCheck.getId())));
  }

  @Test
  void getAllStatusChecks_byStudent_ko() {
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> studentApiAsStudent1().getAllStatusChecks(null));
  }

  private static CreateStatusCheck aCreateStatusCheck(User concernedStudent, User requestingUser) {
    return new CreateStatusCheck()
        .id(randomUUID().toString())
        .description("A created status check")
        .concernedStudentId(concernedStudent.getId())
        .requestingUserId(requestingUser.getId());
  }

  private static StatusCheck aStatusCheck(User concernedStudent, User requestingUser) {
    return StatusCheck.builder()
        .id(randomUUID().toString())
        .description("A status check for student : " + concernedStudent.getRef())
        .concernedStudent(concernedStudent)
        .requestingUser(requestingUser)
        .creationDatetime(Instant.now())
        .updateDatetime(Instant.now())
        .result(PENDING)
        .build();
  }

  private static StatusCheck aStatusCheck(
      User concernedStudent, User requestingUser, StatusCheckResult result) {
    return StatusCheck.builder()
        .id(randomUUID().toString())
        .description("A status check for student : " + concernedStudent.getRef())
        .concernedStudent(concernedStudent)
        .requestingUser(requestingUser)
        .creationDatetime(Instant.now())
        .updateDatetime(Instant.now())
        .result(result)
        .build();
  }

  private static UpdateStatusCheck anUpdateStatusCheck() {
    return new UpdateStatusCheck().description("Updated content").result(WITHDRAWN);
  }

  private StudentApi studentApiAsManager() {
    return new StudentApi(anApiClient(MANAGER1_TOKEN, localPort));
  }

  private StudentApi studentApiAsAdmin() {
    return new StudentApi(anApiClient(ADMIN1_TOKEN, localPort));
  }

  private StudentApi studentApiAsTeacher() {
    return new StudentApi(anApiClient(TEACHER1_TOKEN, localPort));
  }

  private StudentApi studentApiAsStudent1() {
    return new StudentApi(anApiClient(STUDENT1_TOKEN, localPort));
  }

  private StudentApi studentApiAsAxel() {
    return new StudentApi(anApiClient(STUDENT_AXEL_TOKEN, localPort));
  }
}
