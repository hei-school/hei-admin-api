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
import static school.hei.haapi.integration.conf.TestUtils.monitor1Link;
import static school.hei.haapi.integration.conf.TestUtils.monitor2Link;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.student1;
import static school.hei.haapi.integration.conf.TestUtils.student2;
import static school.hei.haapi.integration.test_data.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;

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
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.LinkStudentsByMonitorIdRequest;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.security.casdoorAuthentication.config.CertificateLoader;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@AutoConfigureMockMvc
public class MonitoringStudentIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private UserMapper userMapper;
  @Autowired UserRepository userRepository;
  @Autowired MonitoringStudentRepository monitoringStudentRepository;

  private User studentAxel = axel();
  private User studentTolojanahary = tolojanahary();
  private User monitorOfAxel = monitorOfAxel();

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  public void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();
  }

  private void setUpTestData() {
    studentAxel = axel();
    studentTolojanahary = tolojanahary();
    monitorOfAxel = monitorOfAxel();
    userRepository.saveAll(List.of(monitorOfAxel));
    userRepository.saveAll(List.of(studentAxel, studentTolojanahary));
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfAxel.getId(), studentAxel.getId());
  }

  @Test
  void reassign_axelMonitor_ko() {
    var api = new MonitoringApi(anApiClient(MANAGER1_TOKEN));
    var monitorId = monitorOfAxel.getId();
    var studentId = studentAxel.getId();
    var exceptedException =
        "Student with id %s can't be link with the monitor with id %s"
            .formatted(studentId, monitorId);
    var linkStudentsByMonitorIdRequest =
        new LinkStudentsByMonitorIdRequest().studentsIds(List.of(studentId));

    assertBadRequestException(
        exceptedException,
        () -> api.linkStudentsByMonitorId(monitorId, linkStudentsByMonitorIdRequest));
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
