package school.hei.haapi.integration;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.Whoami.RoleEnum.MONITOR;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.integration.testData.StudentTestData.manitra;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;
import static school.hei.haapi.model.User.Status.ALUMNI;
import static school.hei.haapi.model.User.Status.SUSPENDED;

import jakarta.servlet.FilterChain;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.util.matcher.RequestMatcher;
import school.hei.haapi.endpoint.rest.api.FilesApi;
import school.hei.haapi.endpoint.rest.api.SecurityApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Whoami;
import school.hei.haapi.endpoint.rest.security.AlumniStudentFilter;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;

class SecurityIT extends FacadeITMockedThirdParties {
  private static final String ACCESS_DENIED_BODY =
      "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}";

  @Mock private RequestMatcher requestMatcher;
  @Mock private FilterChain filterChain;
  @InjectMocks private AlumniStudentFilter filter;
  @Autowired private UserRepository userRepository;

  private User studentAxel;
  private User monitorAxel;
  private User teacherToky;
  private User managerHasina;
  private User alumniStudent;
  private User suspendedStudent;

  private String axelToken;
  private String monitorToken;
  private String teacherToken;
  private String managerToken;
  private String alumniToken;
  private String suspendedToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    teacherToky = userRepository.save(toky());
    managerHasina = userRepository.save(hasina());
    monitorAxel = userRepository.save(monitorOfAxel());

    alumniStudent = manitra();
    alumniStudent.setStatus(ALUMNI);
    alumniStudent = userRepository.save(alumniStudent);

    suspendedStudent = freddy();
    suspendedStudent.setStatus(SUSPENDED);
    suspendedStudent = userRepository.save(suspendedStudent);
  }

  @BeforeEach
  public void setUp() {
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    monitorToken = tokenFor(casdoorAuthServiceMock, monitorAxel);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    alumniToken = tokenFor(casdoorAuthServiceMock, alumniStudent);
    suspendedToken = tokenFor(casdoorAuthServiceMock, suspendedStudent);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll(
        List.of(
            studentAxel, monitorAxel, teacherToky, managerHasina, alumniStudent, suspendedStudent));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private HttpResponse<String> get(String path, String token)
      throws IOException, InterruptedException {
    return HttpClient.newBuilder()
        .build()
        .send(
            HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + localPort + path))
                .header("Authorization", "Bearer " + token)
                .build(),
            HttpResponse.BodyHandlers.ofString());
  }

  private static Whoami aWhoami(User user, String bearer, Whoami.RoleEnum role) {
    var whoami = new Whoami();
    whoami.setId(user.getId());
    whoami.setBearer(bearer);
    whoami.setRole(role);
    return whoami;
  }

  @Test
  void alumni_read_whoami_ok() throws ApiException {
    var actual = new SecurityApi(anApiClient(alumniToken)).whoami();

    assertEquals(aWhoami(alumniStudent, alumniToken, Whoami.RoleEnum.STUDENT), actual);
  }

  @Test
  void non_authorized_path_for_alumni_user_ko() {
    var api = new FilesApi(anApiClient(alumniToken));

    var exception =
        assertThrows(
            ApiException.class, () -> api.getStudentScholarshipCertificate(alumniStudent.getId()));

    assertEquals(HttpStatus.FORBIDDEN.value(), exception.getCode());
  }

  @Test
  void monitor_read_whoami_ok() throws ApiException {
    var actual = new SecurityApi(anApiClient(monitorToken)).whoami();

    assertEquals(aWhoami(monitorAxel, monitorToken, MONITOR), actual);
  }

  @Test
  void student_read_whoami_ok() throws ApiException {
    var actual = new SecurityApi(anApiClient(axelToken)).whoami();

    assertEquals(aWhoami(studentAxel, axelToken, Whoami.RoleEnum.STUDENT), actual);
  }

  @Test
  void teacher_read_whoami_ok() throws ApiException {
    var actual = new SecurityApi(anApiClient(teacherToken)).whoami();

    assertEquals(aWhoami(teacherToky, teacherToken, Whoami.RoleEnum.TEACHER), actual);
  }

  @Test
  void manager_read_whoami_ok() throws ApiException {
    var actual = new SecurityApi(anApiClient(managerToken)).whoami();

    assertEquals(aWhoami(managerHasina, managerToken, Whoami.RoleEnum.MANAGER), actual);
  }

  @Test
  void manager_read_unknown_ko() throws IOException, InterruptedException {
    var response = get("/unknown", managerToken);

    assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
    assertEquals(ACCESS_DENIED_BODY, response.body());
  }

  @Test
  void non_authorized_path_for_suspended_user_ko() {
    var nonAuthorizedPaths =
        List.of(
            "/school/files",
            "/school/files/*",
            "/students/*/work_files",
            "/students/*/work_files/*",
            "/students/*/files",
            "/students/*/files/*",
            "/students/*/scholarship_certificate/raw",
            "/announcements",
            "/announcements/*");

    nonAuthorizedPaths.forEach(
        path -> {
          HttpResponse<String> response;
          try {
            response = get(path, suspendedToken);
          } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
          }

          assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
          assertEquals(ACCESS_DENIED_BODY, response.body());
        });
  }

  @Test
  void suspended_read_ko() throws IOException, InterruptedException {
    var response = get("/non-accessible-by-suspended", suspendedToken);

    assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
    assertEquals(ACCESS_DENIED_BODY, response.body());
  }

  @Test
  void should_return_403_when_user_is_alumni() throws Exception {
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();

    when(requestMatcher.matches(request)).thenReturn(true);

    var principal = mock(Principal.class);
    when(principal.getStatus()).thenReturn(ALUMNI);

    try (MockedStatic<AuthProvider> mocked = mockStatic(AuthProvider.class)) {
      mocked.when(AuthProvider::getPrincipal).thenReturn(principal);
      filter.doFilterInternal(request, response, filterChain);
    }
    assertEquals(SC_FORBIDDEN, response.getStatus());
  }
}
