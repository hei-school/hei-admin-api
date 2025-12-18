package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.AXEL_MONITOR_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR2_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT3_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertBadRequestException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.getCasdoorUserMonitor1;
import static school.hei.haapi.integration.conf.TestUtils.monitor1Link;
import static school.hei.haapi.integration.conf.TestUtils.monitor2Link;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.student1;
import static school.hei.haapi.integration.conf.TestUtils.student2;
import static school.hei.haapi.integration.test_data.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.test_data.MonitorTestData.monitorOfFreddy;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.freddy;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;
import static school.hei.haapi.model.dto.MonitorStudentLinkDto.Status.LINKED;

import java.util.List;
import org.casbin.casdoor.entity.CasdoorRole;
import org.casbin.casdoor.entity.CasdoorUser;
import org.casbin.casdoor.service.CasdoorAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.MonitoringApi;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.endpoint.rest.security.casdoorAuthentication.config.CertificateLoader;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@AutoConfigureMockMvc
class MonitoringStudentIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private UserMapper userMapper;
  @Autowired UserRepository userRepository;
  @Autowired MonitoringStudentRepository monitoringStudentRepository;

  private final User studentAxel = axel();
  private final User studentTolojanahary = tolojanahary();
  private final User studentFreddy = freddy();
  private final User monitorOfAxel = monitorOfAxel();
  private final User monitorOfFreddy = monitorOfFreddy();
  private final String FREDDY_MONITOR_TOKEN = "freddy-monitor-token";

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  public void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpFreddyMonitorCasdoorUser(casdoorAuthServiceMock);
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();
  }

  private void setUpTestData() {
    userRepository.saveAll(List.of(monitorOfAxel, monitorOfFreddy));
    userRepository.saveAll(List.of(studentAxel, studentTolojanahary, studentFreddy));
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfAxel.getId(), List.of(studentAxel.getId()), LINKED.toString());
  }

  private void setUpFreddyMonitorCasdoorUser(CasdoorAuthService authService) {
    when(authService.parseJwtToken(FREDDY_MONITOR_TOKEN)).thenReturn(getFreddyMonitorUser());
  }

  private CasdoorUser getFreddyMonitorUser() {
    var user = getCasdoorUserMonitor1();
    user.setEmail(monitorOfFreddy.getEmail());
    return user;
  }

  @Test
  void reassign_axelMonitor_ko() {
    var api = new MonitoringApi(anApiClient(MANAGER1_TOKEN));
    var monitorId = monitorOfAxel.getId();
    var studentToLinkId = List.of(studentAxel.getId());
    var exceptedException =
        "One of the students with id %s can't be link with the monitor with id %s"
            .formatted(studentToLinkId, monitorId);
    var linkStudentsByMonitorIdRequest =
        new LinkStudentsByMonitorIdRequest().studentsIds(studentToLinkId);

    assertBadRequestException(
        exceptedException,
        () -> api.linkStudentsByMonitorId(monitorId, linkStudentsByMonitorIdRequest));
  }

  @Test
  void student_monitor_validation_ok() throws ApiException {
    var api = new MonitoringApi(anApiClient(FREDDY_MONITOR_TOKEN));
    var managerApi = new MonitoringApi(anApiClient(MANAGER1_TOKEN));

    api.linkStudentsByMonitorId(
        monitorOfFreddy.getId(),
        new LinkStudentsByMonitorIdRequest().studentsIds(List.of(studentFreddy.getId())));
    var linkRequests = managerApi.getLinkStudentRequests(1, 10);
    assertEquals(1, linkRequests.size());
    assertEquals(0, api.getLinkedStudentsByMonitorId(monitorOfFreddy.getId(), 1, 10).size());

    managerApi.updateMonitorStudentLinkStatus(
        new UpdateMonitorStudentLinkStatusRequest()
            .monitorStudentLink(
                linkRequests.stream()
                    .map(
                        e ->
                            new UpdateMonitorStudentLink()
                                .id(e.getId())
                                .studentId(e.getMonitorId())
                                .monitorId(e.getMonitorId())
                                .status(MonitorStudentLinkStatus.LINKED))
                    .toList()));

    var emptyLinkRequests = managerApi.getLinkStudentRequests(1, 10);
    assertEquals(0, emptyLinkRequests.size());
    assertEquals(1, api.getLinkedStudentsByMonitorId(monitorOfFreddy.getId(), 1, 10).size());
  }

  @Test
  @Disabled("Dirty")
  void monitor_follow_students_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    MonitoringApi api = new MonitoringApi(manager1Client);
    UsersApi usersApi = new UsersApi(manager1Client);

    // 1. Link some students to a monitor ...
    usersApi.createOrUpdateMonitors(List.of(monitor2Link(someStudentsRefsToLinkToAMonitor())));
    List<Student> studentsLinked = api.getLinkedStudentsByMonitorId(MONITOR2_ID, 1, 10);

    assertEquals(2, studentsLinked.size());
    assertTrue(studentsLinked.containsAll(List.of(student1(), student2())));

    // 2. ... Except that the monitor access to his resources ...
    ApiClient monitor2Client = anApiClient(MONITOR2_TOKEN);
    PayingApi payingApi = new PayingApi(monitor2Client);
    List<Fee> followedStudentFee = payingApi.getStudentFees(STUDENT2_ID, 1, 10, null);

    assertFalse(followedStudentFee.isEmpty());

    // 3. ... And except that for other student monitor doesn't have access
    assertThrowsForbiddenException(() -> payingApi.getStudentFees(STUDENT3_ID, 1, 10, null));
  }

  @Test
  @Disabled("Other tests outside this class alters data")
  void manager_read_students_followed_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    MonitoringApi api = new MonitoringApi(manager1Client);

    List<Student> studentsLinkedToAMonitor = api.getLinkedStudentsByMonitorId(MONITOR1_ID, 1, 10);
    assertEquals(1, studentsLinkedToAMonitor.size());
    assertEquals(student1(), studentsLinkedToAMonitor.getFirst());
  }

  @Test
  void monitor_follow_students_ko() {
    ApiClient teacher1client = anApiClient(TEACHER1_TOKEN);
    UsersApi teacherApi = new UsersApi(teacher1client);

    ApiClient student1client = anApiClient(STUDENT1_TOKEN);
    UsersApi studentApi = new UsersApi(student1client);

    assertThrowsForbiddenException(
        () ->
            teacherApi.createOrUpdateMonitors(
                List.of(monitor1Link(someStudentsRefsToLinkToAMonitor()))));
    assertThrowsForbiddenException(
        () ->
            studentApi.createOrUpdateMonitors(
                List.of(monitor1Link(someStudentsRefsToLinkToAMonitor()))));
  }

  public static List<String> someStudentsRefsToLinkToAMonitor() {
    return List.of(student1().getRef(), student2().getRef());
  }

  @Test
  void monitor_get_monitored_student_ok() throws ApiException {
    setUpCasdoorMonitor(casdoorAuthServiceMock, certificateLoaderMock, monitorOfAxel);
    MonitoringApi api = new MonitoringApi(anApiClient(AXEL_MONITOR_TOKEN));

    var actualStudent =
        api.getLinkedStudentByIdAndMonitorId(monitorOfAxel.getId(), studentAxel.getId());

    assertEquals(userMapper.toRestStudent(studentAxel), actualStudent);
  }

  @Test
  void monitor_get_non_monitored_students_ko() {
    setUpCasdoorMonitor(casdoorAuthServiceMock, certificateLoaderMock, monitorOfAxel);
    MonitoringApi api = new MonitoringApi(anApiClient(AXEL_MONITOR_TOKEN));

    assertThrowsForbiddenException(
        () ->
            api.getLinkedStudentByIdAndMonitorId(
                monitorOfAxel.getId(), studentTolojanahary.getId()));
  }

  @Test
  void teacher_getStudentByIdAndMonitorId_ko() {
    MonitoringApi api = new MonitoringApi(anApiClient(TEACHER1_TOKEN));

    assertThrowsForbiddenException(
        () -> api.getLinkedStudentByIdAndMonitorId(monitorOfAxel.getId(), studentAxel.getId()));
  }

  @Test
  void manager_or_admin_getStudentByIdAndMonitorId_ok() throws ApiException {
    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    ApiClient adminClient = anApiClient(ADMIN1_TOKEN);
    MonitoringApi managerApi = new MonitoringApi(managerClient);
    MonitoringApi adminApi = new MonitoringApi(adminClient);

    var studentAsManager =
        managerApi.getLinkedStudentByIdAndMonitorId(monitorOfAxel.getId(), studentAxel.getId());
    var studentAsAdmin =
        adminApi.getLinkedStudentByIdAndMonitorId(monitorOfAxel.getId(), studentAxel.getId());

    assertEquals(userMapper.toRestStudent(studentAxel), studentAsAdmin);
    assertEquals(userMapper.toRestStudent(studentAxel), studentAsManager);
  }

  private void setUpCasdoorMonitor(
      CasdoorAuthService casdoorAuthService, CertificateLoader certificateLoader, User monitor) {
    given(certificateLoader.getCertificate()).willReturn("mocked-certificate");
    when(casdoorAuthService.parseJwtToken(AXEL_MONITOR_TOKEN))
        .thenReturn(getCasdoorUserFromMonitor(monitor));
  }

  private CasdoorUser getCasdoorUserFromMonitor(User monitor) {
    CasdoorUser user = new CasdoorUser();
    user.setEmail(monitor.getEmail());
    CasdoorRole casdoorRole = new CasdoorRole();
    casdoorRole.setOwner("dummy");
    casdoorRole.setName("student");
    String[] roleUsers = List.of("dummy/user").toArray(new String[0]);
    casdoorRole.setUsers(roleUsers);
    user.setRoles(List.of(casdoorRole));

    return user;
  }
}
