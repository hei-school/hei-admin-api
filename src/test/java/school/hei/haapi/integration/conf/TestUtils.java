package school.hei.haapi.integration.conf;

import org.junit.jupiter.api.function.Executable;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class TestUtils {

  public static final String STUDENT1_ID = "student1_id";
  public static final String STUDENT2_ID = "student2_id";
  public static final String TEACHER1_ID = "teacher1_id";
  public static final String TEACHER2_ID = "teacher2_id";
  public static final String GROUP1_ID = "group1_id";

  public static final String BAD_TOKEN = "bad_token";
  public static final String STUDENT1_TOKEN = "student1_token";
  public static final String TEACHER1_TOKEN = "teacher1_token";
  public static final String MANAGER1_TOKEN = "manager1_token";

  public static ApiClient anApiClient(String token, int serverPort) {
    ApiClient client = new ApiClient();
    client.setScheme("http");
    client.setHost("localhost");
    client.setPort(serverPort);
    client.setRequestInterceptor(httpRequestBuilder ->
        httpRequestBuilder.header("Authorization", "Bearer " + token));
    return client;
  }

  public static void setUpCognito(CognitoComponent cognitoComponent) {
    when(cognitoComponent.getEmailByBearer(BAD_TOKEN)).thenReturn(null);
    when(cognitoComponent.getEmailByBearer(STUDENT1_TOKEN)).thenReturn("ryan@hei.school");
    when(cognitoComponent.getEmailByBearer(TEACHER1_TOKEN)).thenReturn("teacher1@hei.school");
    when(cognitoComponent.getEmailByBearer(MANAGER1_TOKEN)).thenReturn("manager1@hei.school");
  }

  public static void assertThrowsApiException(String expectedBody, Executable executable) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    assertEquals(expectedBody.trim(), apiException.getResponseBody().trim());
  }

  public static boolean isValidUUID(String candidate) {
    try {
      UUID.fromString(candidate);
      return true;
    } catch (Exception e){
      return false;
    }
  }

  public static int anAvailableRandomPort() {
    try {
      return new ServerSocket(0).getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
