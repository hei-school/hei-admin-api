package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.client.HaHttpClient;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.integration.conf.AuthorizationContextInitializer;
import school.hei.haapi.integration.conf.CallerData;
import school.hei.haapi.security.cognito.CognitoComponent;
import school.hei.haapi.web.model.StudentResource;
import school.hei.haapi.web.model.TeacherResource;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = AuthorizationContextInitializer.class)
@AutoConfigureMockMvc
class AuthorizationIntegrationTest {

  private final int serverPort;
  @MockBean private CognitoComponent cognitoComponent;

  public AuthorizationIntegrationTest(@LocalServerPort int serverPort) {
    this.serverPort = serverPort;
  }

  private HaHttpClient clientFromToken(String token) {
    return new HaHttpClient("http://localhost:" + serverPort, token);
  }

  @Test
  void student1_can_get_his_information() {
    HaHttpClient student1 = clientFromToken(CallerData.STUDENT1_TOKEN);
    when(cognitoComponent.findEmailByBearer(CallerData.STUDENT1_TOKEN))
        .thenReturn("ryan@hei.school");

    StudentResource studentResource = student1.getStudentById(CallerData.STUDENT1_ID);

    assertEquals("Ryan", studentResource.getFirstName());
  }

  @Test
  void student1_can_not_get_student2_information() {
    HaHttpClient student1 = clientFromToken(CallerData.STUDENT1_TOKEN);
    when(cognitoComponent.findEmailByBearer(CallerData.STUDENT1_TOKEN))
        .thenReturn("ryan@hei.school");

    assertThrows(ForbiddenException.class, () -> student1.getStudentById(CallerData.STUDENT2_ID));
  }

  @Test
  void student1_can_not_get_teacher1_information() {
    HaHttpClient student1 = clientFromToken(CallerData.STUDENT1_TOKEN);
    when(cognitoComponent.findEmailByBearer(CallerData.STUDENT1_TOKEN))
        .thenReturn("ryan@hei.school");

    assertThrows(ForbiddenException.class, () -> student1.getTeacherById(CallerData.TEACHER1_ID));
  }

  @Test
  void teacher1_can_not_get_teacher2_information() {
    HaHttpClient teacher1 = clientFromToken(CallerData.STUDENT1_TOKEN);
    when(cognitoComponent.findEmailByBearer(CallerData.STUDENT1_TOKEN))
        .thenReturn("teacher1@hei.school");

    assertThrows(ForbiddenException.class, () -> teacher1.getTeacherById(CallerData.TEACHER2_ID));
  }

  @Test
  void teacher1_can_get_his_information() {
    HaHttpClient teacher1 = clientFromToken(CallerData.TEACHER1_TOKEN);
    when(cognitoComponent.findEmailByBearer(CallerData.TEACHER1_TOKEN))
        .thenReturn("teacher1@hei.school");

    TeacherResource teacherResource = teacher1.getTeacherById(CallerData.TEACHER1_ID);

    assertEquals("One", teacherResource.getLastName());
  }

  @Test
  void teacher1_can_get_student1_information() {
    HaHttpClient teacher1 = clientFromToken(CallerData.TEACHER1_TOKEN);
    when(cognitoComponent.findEmailByBearer(CallerData.TEACHER1_TOKEN))
        .thenReturn("teacher1@hei.school");

    StudentResource studentResource = teacher1.getStudentById(CallerData.STUDENT1_ID);

    assertEquals("Ryan", studentResource.getFirstName());
  }
}
