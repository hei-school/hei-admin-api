package school.hei.haapi.integration;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.ApiAssertions.assertBadRequestException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.testData.FeeTestData.createPendingFee;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfFreddy;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.integration.testData.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;
import static school.hei.haapi.model.dto.MonitorStudentLinkDto.Status.LINKED;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.MonitoringApi;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.CrupdateMonitor;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.LinkStudentsByMonitorIdRequest;
import school.hei.haapi.endpoint.rest.model.MonitorStudentLinkStatus;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.model.UpdateMonitorStudentLink;
import school.hei.haapi.endpoint.rest.model.UpdateMonitorStudentLinkStatusRequest;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class MonitoringStudentIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private UserMapper userMapper;
  @Autowired UserRepository userRepository;
  @Autowired FeeRepository feeRepository;
  @Autowired MonitoringStudentRepository monitoringStudentRepository;

  private User studentAxel;
  private User studentTolojanahary;
  private User studentFreddy;
  private User monitorOfAxel;
  private User monitorOfFreddy;
  private User managerHasina;
  private User adminUser;
  private User teacherToky;
  private Fee freddyFee;

  private String axelMonitorToken;
  private String freddyMonitorToken;
  private String managerToken;
  private String adminToken;
  private String teacherToken;
  private String axelToken;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private void setUpTestData() {
    monitorOfAxel = userRepository.save(monitorOfAxel());
    monitorOfFreddy = userRepository.save(monitorOfFreddy());
    studentAxel = userRepository.save(axel());
    studentTolojanahary = userRepository.save(tolojanahary());
    studentFreddy = userRepository.save(freddy());
    managerHasina = userRepository.save(hasina());
    adminUser = userRepository.save(adminMialy());
    teacherToky = userRepository.save(toky());

    freddyFee = feeRepository.save(createPendingFee(studentFreddy, 5000, now().plus(30, DAYS)));

    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfAxel.getId(), List.of(studentAxel.getId()), LINKED.toString());
  }

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();

    // a monitor authenticates with the student casdoor role
    axelMonitorToken =
        tokenFor(casdoorAuthServiceMock, monitorOfAxel.getEmail(), User.Role.STUDENT);
    freddyMonitorToken =
        tokenFor(casdoorAuthServiceMock, monitorOfFreddy.getEmail(), User.Role.STUDENT);
    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    adminToken = tokenFor(casdoorAuthServiceMock, adminUser);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
  }

  @AfterEach
  void tearDown() {
    clearFollowedStudents(monitorOfAxel.getId());
    clearFollowedStudents(monitorOfFreddy.getId());
    feeRepository.deleteById(freddyFee.getId());
    userRepository.deleteAll(
        List.of(
            studentAxel,
            studentTolojanahary,
            studentFreddy,
            monitorOfAxel,
            monitorOfFreddy,
            managerHasina,
            adminUser,
            teacherToky));
  }

  private void clearFollowedStudents(String monitorId) {
    userRepository
        .findById(monitorId)
        .ifPresent(
            monitor -> {
              monitor.setMonitors(new ArrayList<>());
              userRepository.save(monitor);
            });
  }

  private CrupdateMonitor aMonitorLinkedTo(User monitor, List<String> studentRefs) {
    return new CrupdateMonitor()
        .id(monitor.getId())
        .firstName(monitor.getFirstName())
        .lastName(monitor.getLastName())
        .email(monitor.getEmail())
        .ref(monitor.getRef())
        .phone("0322411123")
        .status(EnableStatus.ENABLED)
        .sex(Sex.M)
        .birthDate(LocalDate.parse("2000-01-01"))
        .entranceDatetime(monitor.getEntranceDatetime())
        .nic("")
        .birthPlace("")
        .address("Adr 1")
        .coordinates(new Coordinates().longitude(-123.123).latitude(123.0))
        .highSchoolOrigin("Lycée Andohalo")
        .studentRefs(studentRefs);
  }

  @Test
  void reassign_axelMonitor_ko() {
    var api = new MonitoringApi(anApiClient(managerToken));
    var monitorId = monitorOfAxel.getId();
    var studentToLinkId = List.of(studentAxel.getId());
    var expectedException =
        "One of the students with id %s can't be link with the monitor with id %s"
            .formatted(studentToLinkId, monitorId);
    var request = new LinkStudentsByMonitorIdRequest().studentsIds(studentToLinkId);

    assertBadRequestException(
        expectedException, () -> api.linkStudentsByMonitorId(monitorId, request));
  }

  @Test
  void student_monitor_validation_ok() throws ApiException {
    var api = new MonitoringApi(anApiClient(freddyMonitorToken));
    var managerApi = new MonitoringApi(anApiClient(managerToken));

    api.linkStudentsByMonitorId(
        monitorOfFreddy.getId(),
        new LinkStudentsByMonitorIdRequest().studentsIds(List.of(studentFreddy.getId())));

    var linkRequests = managerApi.getLinkStudentRequests(1, 100);
    assertTrue(
        linkRequests.stream()
            .anyMatch(r -> monitorOfFreddy.getId().equals(r.getMonitor().getId())));
    assertEquals(0, api.getLinkedStudentsByMonitorId(monitorOfFreddy.getId(), 1, 10).size());

    managerApi.updateMonitorStudentLinkStatus(
        new UpdateMonitorStudentLinkStatusRequest()
            .monitorStudentLink(
                linkRequests.stream()
                    .filter(r -> monitorOfFreddy.getId().equals(r.getMonitor().getId()))
                    .map(
                        e ->
                            new UpdateMonitorStudentLink()
                                .id(e.getId())
                                .studentId(e.getStudent().getId())
                                .monitorId(e.getMonitor().getId())
                                .status(MonitorStudentLinkStatus.LINKED))
                    .toList()));

    assertFalse(
        managerApi.getLinkStudentRequests(1, 100).stream()
            .anyMatch(r -> monitorOfFreddy.getId().equals(r.getMonitor().getId())));
    assertEquals(1, api.getLinkedStudentsByMonitorId(monitorOfFreddy.getId(), 1, 10).size());
  }

  @Test
  void monitor_follow_students_ok() throws ApiException {
    var managerClient = anApiClient(managerToken);
    var api = new MonitoringApi(managerClient);
    var usersApi = new UsersApi(managerClient);

    // 1. link two students to the monitor ...
    usersApi.createOrUpdateMonitors(
        List.of(
            aMonitorLinkedTo(
                monitorOfFreddy, List.of(studentFreddy.getRef(), studentTolojanahary.getRef()))));
    var studentsLinked = api.getLinkedStudentsByMonitorId(monitorOfFreddy.getId(), 1, 10);

    assertEquals(2, studentsLinked.size());
    assertTrue(studentsLinked.stream().anyMatch(s -> studentFreddy.getId().equals(s.getId())));
    assertTrue(
        studentsLinked.stream().anyMatch(s -> studentTolojanahary.getId().equals(s.getId())));

    // 2. ... the monitor reaches the resources of a followed student ...
    var payingApi = new PayingApi(anApiClient(freddyMonitorToken));
    var followedStudentFees = payingApi.getFeesByStudentId(studentFreddy.getId(), 1, 10, null);
    assertFalse(followedStudentFees.isEmpty());

    // 3. ... but not those of a student they do not follow
    assertThrowsForbiddenException(
        () -> payingApi.getFeesByStudentId(studentAxel.getId(), 1, 10, null));
  }

  @Test
  void manager_read_students_followed_ok() throws ApiException {
    var api = new MonitoringApi(anApiClient(managerToken));

    var followed = api.getLinkedStudentsByMonitorId(monitorOfAxel.getId(), 1, 10);

    assertEquals(1, followed.size());
    assertEquals(studentAxel.getId(), followed.getFirst().getId());
  }

  @Test
  void monitor_follow_students_ko() {
    var teacherApi = new UsersApi(anApiClient(teacherToken));
    var studentApi = new UsersApi(anApiClient(axelToken));
    var refs = List.of(studentFreddy.getRef(), studentTolojanahary.getRef());

    assertThrowsForbiddenException(
        () -> teacherApi.createOrUpdateMonitors(List.of(aMonitorLinkedTo(monitorOfFreddy, refs))));
    assertThrowsForbiddenException(
        () -> studentApi.createOrUpdateMonitors(List.of(aMonitorLinkedTo(monitorOfFreddy, refs))));
  }

  @Test
  void monitor_get_monitored_student_ok() throws ApiException {
    var api = new MonitoringApi(anApiClient(axelMonitorToken));

    var actualStudent =
        api.getLinkedStudentByIdAndMonitorId(monitorOfAxel.getId(), studentAxel.getId());

    assertEquals(userMapper.toRestStudent(studentAxel), actualStudent);
  }

  @Test
  void monitor_get_non_monitored_students_ko() {
    var api = new MonitoringApi(anApiClient(axelMonitorToken));

    assertThrowsForbiddenException(
        () ->
            api.getLinkedStudentByIdAndMonitorId(
                monitorOfAxel.getId(), studentTolojanahary.getId()));
  }

  @Test
  void teacher_getStudentByIdAndMonitorId_ko() {
    var api = new MonitoringApi(anApiClient(teacherToken));

    assertThrowsForbiddenException(
        () -> api.getLinkedStudentByIdAndMonitorId(monitorOfAxel.getId(), studentAxel.getId()));
  }

  @Test
  void manager_or_admin_getStudentByIdAndMonitorId_ok() throws ApiException {
    var managerApi = new MonitoringApi(anApiClient(managerToken));
    var adminApi = new MonitoringApi(anApiClient(adminToken));

    var studentAsManager =
        managerApi.getLinkedStudentByIdAndMonitorId(monitorOfAxel.getId(), studentAxel.getId());
    var studentAsAdmin =
        adminApi.getLinkedStudentByIdAndMonitorId(monitorOfAxel.getId(), studentAxel.getId());

    assertEquals(userMapper.toRestStudent(studentAxel), studentAsAdmin);
    assertEquals(userMapper.toRestStudent(studentAxel), studentAsManager);
  }
}
