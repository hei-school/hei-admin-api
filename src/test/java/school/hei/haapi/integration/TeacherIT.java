package school.hei.haapi.integration;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.ApiAssertions.assertBadRequestException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.ApiAssertions.isValidUUID;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCreatableTeacher;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestFiles.uploadProfilePicture;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.TeacherTestData.disabledFemaleTeacher;
import static school.hei.haapi.integration.testData.TeacherTestData.harry;
import static school.hei.haapi.integration.testData.TeacherTestData.ryan;
import static school.hei.haapi.integration.testData.TeacherTestData.suspendedFemaleTeacher;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;

class TeacherIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserMapper userMapper;
  @Autowired private UserRepository userRepository;

  private User teacherToky;
  private User teacherRyan;
  private User teacherHarry;
  private User teacherDisabledFemale;
  private User teacherSuspendedFemale;
  private User managerHasina;
  private User studentAxel;

  private String tokyToken;
  private String managerToken;
  private String axelToken;

  private void setUpTestData() {
    teacherToky = userRepository.save(toky());
    teacherRyan = userRepository.save(ryan());
    teacherHarry = userRepository.save(harry());
    teacherDisabledFemale = userRepository.save(disabledFemaleTeacher());
    teacherSuspendedFemale = userRepository.save(suspendedFemaleTeacher());
    managerHasina = userRepository.save(hasina());
    studentAxel = userRepository.save(axel());
  }

  @BeforeEach
  public void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();
    setUpS3Service(fileService, teacherToky);

    tokyToken = tokenFor(casdoorAuthServiceMock, teacherToky);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll(
        List.of(
            teacherToky,
            teacherRyan,
            teacherHarry,
            teacherDisabledFemale,
            teacherSuspendedFemale,
            managerHasina,
            studentAxel));
  }

  private UsersApi apiAs(String token) {
    return new UsersApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private static List<String> idsOf(List<Teacher> teachers) {
    return teachers.stream().map(Teacher::getId).toList();
  }

  @Test
  void teacher_update_own_profile_picture() throws IOException, InterruptedException {
    var response = uploadProfilePicture(localPort, tokyToken, teacherToky.getId(), "teachers");
    var teacher = objectMapper.readValue(response.body(), Teacher.class);

    assertEquals(200, response.statusCode());
    assertEquals(teacherToky.getRef(), teacher.getRef());
  }

  @Test
  void student_read_ko() {
    var api = apiAs(axelToken);

    assertThrowsForbiddenException(() -> api.getTeacherById(teacherToky.getId()));
    assertThrowsForbiddenException(() -> api.getTeachers(1, 20, null, null, null, null, null));
  }

  @Test
  void teacher_read_ko() {
    var api = apiAs(tokyToken);
    assertThrowsForbiddenException(() -> api.getTeacherById(teacherRyan.getId()));
    assertThrowsForbiddenException(() -> api.getTeachers(1, 20, null, null, null, null, null));
  }

  @Test
  void student_write_ko() {
    var api = apiAs(axelToken);
    assertThrowsForbiddenException(() -> api.createOrUpdateTeachers(List.of()));
  }

  @Test
  void teacher_write_ko() {
    var api = apiAs(tokyToken);
    assertThrowsForbiddenException(() -> api.createOrUpdateTeachers(List.of()));
  }

  @Test
  void teacher_read_own_ok() throws ApiException {
    var actual = apiAs(tokyToken).getTeacherById(teacherToky.getId());

    assertEquals(teacherToky.getId(), actual.getId());
    assertEquals(teacherToky.getRef(), actual.getRef());
    assertEquals(teacherToky.getFirstName(), actual.getFirstName());
  }

  @Test
  void manager_read_ok() throws ApiException {
    var teachers = apiAs(managerToken).getTeachers(1, 100, null, null, null, null, null);

    assertTrue(idsOf(teachers).contains(teacherToky.getId()));
    assertTrue(idsOf(teachers).contains(teacherRyan.getId()));
  }

  @Test
  void manager_write_update_rollback_on_event_error() throws ApiException {
    var api = apiAs(managerToken);
    var toCreate = someCreatableTeacher();
    reset(eventBridgeClientMock);
    when(eventBridgeClientMock.putEvents((PutEventsRequest) any()))
        .thenThrow(RuntimeException.class);

    assertThrowsApiException(
        "{\"type\":\"500 INTERNAL_SERVER_ERROR\",\"message\":null}",
        () -> api.createOrUpdateTeachers(List.of(toCreate)));

    var actual = api.getTeachers(1, 100, null, null, null, null, null);
    assertFalse(actual.stream().anyMatch(s -> Objects.equals(toCreate.getEmail(), s.getEmail())));
  }

  @Test
  void manager_write_create_ok() throws ApiException {
    var api = apiAs(managerToken);
    var toCreate = someCreatableTeacher();
    var expected = userMapper.toRestTeacher(userMapper.toDomain(toCreate));

    var created = api.createOrUpdateTeachers(List.of(toCreate));
    assertEquals(1, created.size());

    var created0 = created.getFirst();
    assertTrue(isValidUUID(created0.getId()));
    expected.setId(created0.getId());
    expected.setRef(toCreate.getRef());
    expected.setEmail(toCreate.getEmail());
    assertEquals(expected, created0);

    userRepository.deleteById(created0.getId());
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    var api = apiAs(managerToken);
    var toUpdate = someCreatableTeacher();

    var created = api.createOrUpdateTeachers(List.of(toUpdate));
    toUpdate.setId(created.getFirst().getId());

    var expected = userMapper.toRestTeacher(userMapper.toDomain(toUpdate));
    expected.setId(created.getFirst().getId());
    expected.setLastName("New last name");
    expected.setEmail(toUpdate.getEmail());
    expected.setRef(toUpdate.getRef());

    toUpdate.setLastName("New last name");

    var updated = api.createOrUpdateTeachers(List.of(toUpdate));

    assertEquals(1, updated.size());
    assertEquals(expected, updated.getFirst());

    userRepository.deleteById(created.getFirst().getId());
  }

  @Test
  void manager_write_update_with_some_bad_fields_ko() {
    var api = apiAs(managerToken);
    var missingFields =
        someCreatableTeacher()
            .firstName(null)
            .lastName(null)
            .email(null)
            .address(null)
            .phone(null)
            .ref(null);
    var badEmail = someCreatableTeacher().email("bademail");

    var missingFieldsException =
        assertThrows(ApiException.class, () -> api.createOrUpdateTeachers(List.of(missingFields)));
    var badEmailException =
        assertThrows(ApiException.class, () -> api.createOrUpdateTeachers(List.of(badEmail)));
    assertBadRequestException(
        "Entrance datetime is mandatory",
        () -> api.createOrUpdateTeachers(List.of(someCreatableTeacher().entranceDatetime(null))));

    assertTrue(badEmailException.getMessage().contains("Email must be valid"));
    assertTrue(missingFieldsException.getMessage().contains("Last name is mandatory"));
    assertTrue(missingFieldsException.getMessage().contains("Email is mandatory"));
    assertTrue(missingFieldsException.getMessage().contains("Reference is mandatory"));
  }

  @Test
  void manager_read_by_disabled_status_ok() throws ApiException {
    var actualTeachers =
        apiAs(managerToken).getTeachers(1, 100, null, null, null, EnableStatus.DISABLED, null);

    assertTrue(idsOf(actualTeachers).contains(teacherHarry.getId()));
    assertTrue(idsOf(actualTeachers).contains(teacherDisabledFemale.getId()));
    assertFalse(idsOf(actualTeachers).contains(teacherToky.getId()));
  }

  @Test
  void manager_read_by_suspended_status_ok() throws ApiException {
    var actualTeachers =
        apiAs(managerToken).getTeachers(1, 100, null, null, null, EnableStatus.SUSPENDED, Sex.F);

    assertTrue(idsOf(actualTeachers).contains(teacherSuspendedFemale.getId()));
    assertFalse(idsOf(actualTeachers).contains(teacherDisabledFemale.getId()));
  }

  @Test
  void manager_read_by_status_and_sex_ok() throws ApiException {
    var actualTeachers =
        apiAs(managerToken).getTeachers(1, 100, null, null, null, EnableStatus.DISABLED, Sex.F);

    assertTrue(idsOf(actualTeachers).contains(teacherDisabledFemale.getId()));
    assertFalse(idsOf(actualTeachers).contains(teacherHarry.getId()));
  }

  @Test
  void generate_all_teacher_as_xlsx() throws IOException, InterruptedException {
    var response = getTeachersRaw(managerToken);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
  }

  @Test
  void student_not_authorized_to_generate_all_teacher_as_xlsx()
      throws IOException, InterruptedException {
    var response = getTeachersRaw(axelToken);

    assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
  }

  private HttpResponse<byte[]> getTeachersRaw(String token)
      throws IOException, InterruptedException {
    return HttpClient.newBuilder()
        .build()
        .send(
            HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + localPort + "/teachers/raw"))
                .GET()
                .header("Authorization", "Bearer " + token)
                .build(),
            HttpResponse.BodyHandlers.ofByteArray());
  }
}
