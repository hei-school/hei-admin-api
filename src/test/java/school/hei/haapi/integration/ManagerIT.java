package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestFiles.uploadProfilePicture;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.ManagerTestData.njiva;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Manager;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;

public class ManagerIT extends FacadeITMockedThirdParties {
  @Autowired ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;

  private User managerHasina;
  private User managerNjiva;
  private User studentAxel;
  private User teacherToky;

  private String managerToken;
  private String studentToken;
  private String teacherToken;

  private void setUpTestData() {
    managerHasina = userRepository.save(hasina());
    managerNjiva = userRepository.save(njiva());
    studentAxel = userRepository.save(axel());
    teacherToky = userRepository.save(toky());
  }

  @BeforeEach
  public void setUp() {
    setUpTestData();
    setUpS3Service(fileService, managerHasina);

    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    studentToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll(List.of(managerHasina, managerNjiva, studentAxel, teacherToky));
  }

  private UsersApi apiAs(String token) {
    return new UsersApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @Test
  void manager_update_own_profile_picture() throws IOException, InterruptedException {
    var response = uploadProfilePicture(localPort, managerToken, managerHasina.getId(), "managers");

    var manager = objectMapper.readValue(response.body(), Manager.class);

    assertEquals(200, response.statusCode());
    assertEquals(managerHasina.getRef(), manager.getRef());
  }

  @Test
  void student_read_ko() {
    var api = apiAs(studentToken);

    assertThrowsForbiddenException(() -> api.getManagerById(managerHasina.getId()));
    assertThrowsForbiddenException(() -> api.getManagers(1, 20, null, null));
  }

  @Test
  void teacher_read_ko() {
    var api = apiAs(teacherToken);

    assertThrowsForbiddenException(() -> api.getManagerById(managerHasina.getId()));
    assertThrowsForbiddenException(() -> api.getManagers(1, 20, null, null));
  }

  @Test
  void manager_read_own_ok() throws ApiException {
    var actual = apiAs(managerToken).getManagerById(managerHasina.getId());

    assertEquals(managerHasina.getId(), actual.getId());
    assertEquals(managerHasina.getRef(), actual.getRef());
    assertEquals(managerHasina.getEmail(), actual.getEmail());
  }

  @Test
  void manager_read_ok() throws ApiException {
    var managers = apiAs(managerToken).getManagers(1, 100, null, null);

    assertTrue(managers.stream().anyMatch(m -> managerHasina.getId().equals(m.getId())));
    assertTrue(managers.stream().anyMatch(m -> managerNjiva.getId().equals(m.getId())));
  }
}
