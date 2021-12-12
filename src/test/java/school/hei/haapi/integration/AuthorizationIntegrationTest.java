package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.StudentsApi;
import school.hei.haapi.endpoint.rest.api.TeachersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.CallerData;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.Teacher;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = AuthorizationIntegrationTest.ContextInitializer.class)
@AutoConfigureMockMvc
class AuthorizationIntegrationTest {

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = 8081;

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }

  @MockBean private CognitoComponent cognitoComponent;

  private ApiClient clientFromToken(String token) {
    ApiClient client = new ApiClient();
    client.setHttpClient(new OkHttpClient.Builder()
        .addInterceptor(chain -> chain.proceed(
            chain.request()
                .newBuilder().addHeader("Authorization", "Bearer " + token)
                .build()))
        .build());
    client.setBasePath("http://localhost:" + AuthorizationIntegrationTest.ContextInitializer.SERVER_PORT);
    return client;
  }

  @Test
  void student1_can_get_his_information() throws ApiException {
    ApiClient student1Client = clientFromToken(CallerData.STUDENT1_TOKEN);
    when(cognitoComponent.findEmailByBearer(CallerData.STUDENT1_TOKEN))
        .thenReturn("ryan@hei.school");

    StudentsApi api = new StudentsApi(student1Client);
    Student student = api.findStudentById("TODO:school1", CallerData.STUDENT1_ID);

    assertEquals("Ryan", student.getFirstName());
  }

  @Test
  void student1_can_not_get_student2_information() {
    ApiClient student1Client = clientFromToken(CallerData.STUDENT1_TOKEN);
    when(cognitoComponent.findEmailByBearer(CallerData.STUDENT1_TOKEN))
        .thenReturn("ryan@hei.school");

    StudentsApi api = new StudentsApi(student1Client);
    assertThrowsApiException(
        () -> api.findStudentById("TODO:school1", CallerData.STUDENT2_ID),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Students can only get their own information\"}");
  }

  @Test
  void student1_can_not_get_teacher1_information() {
    ApiClient student1Client = clientFromToken(CallerData.STUDENT1_TOKEN);
    when(cognitoComponent.findEmailByBearer(CallerData.STUDENT1_TOKEN))
        .thenReturn("ryan@hei.school");

    TeachersApi api = new TeachersApi(student1Client);
    assertThrowsApiException(
        () -> api.findTeacherById("TODO:school1", CallerData.TEACHER1_ID),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Students are not allowed to access teacher information\"}");
  }

  @Test
  void teacher1_can_not_get_teacher2_information() {
    ApiClient teacher1Client = clientFromToken(CallerData.TEACHER1_TOKEN);
    when(cognitoComponent.findEmailByBearer(CallerData.TEACHER1_TOKEN))
        .thenReturn("teacher1@hei.school");

    TeachersApi api = new TeachersApi(teacher1Client);
    assertThrowsApiException(
        () -> api.findTeacherById("TODO:school1", CallerData.TEACHER2_ID),
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"A teacher cannot access information of another teacher\"}");  }

  @Test
  void teacher1_can_get_his_information() throws ApiException {
    ApiClient teacher1Client = clientFromToken(CallerData.TEACHER1_TOKEN);
    when(cognitoComponent.findEmailByBearer(CallerData.TEACHER1_TOKEN))
        .thenReturn("teacher1@hei.school");

    TeachersApi api = new TeachersApi(teacher1Client);
    Teacher teacher = api.findTeacherById("TODO:school1", CallerData.TEACHER1_ID);

    assertEquals("One", teacher.getLastName());
  }

  @Test
  void teacher1_can_get_student1_information() throws ApiException {
    ApiClient teacher1Client = clientFromToken(CallerData.TEACHER1_TOKEN);
    when(cognitoComponent.findEmailByBearer(CallerData.TEACHER1_TOKEN))
        .thenReturn("teacher1@hei.school");

    StudentsApi api = new StudentsApi(teacher1Client);
    Student student = api.findStudentById("TODO:school1", CallerData.STUDENT1_ID);

    assertEquals("Ryan", student.getFirstName());
  }

  private void assertThrowsApiException(Executable executable, String expectedBody) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    assertEquals(apiException.getResponseBody(), expectedBody);
  }
}
