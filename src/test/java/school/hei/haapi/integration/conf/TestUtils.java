package school.hei.haapi.integration.conf;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;
import org.junit.jupiter.api.function.Executable;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestUtils {

  public static final String STUDENT1_ID = "student1_id";
  public static final String STUDENT2_ID = "student2_id";
  public static final String TEACHER1_ID = "teacher1_id";
  public static final String TEACHER2_ID = "teacher2_id";
  public static final String MANAGER_ID = "manager1_id";
  public static final String GROUP1_ID = "group1_id";
  public static final String FEE1_ID = "fee1_id";
  public static final String FEE2_ID = "fee2_id";
  public static final String FEE3_ID = "fee3_id";
  public static final String FEE4_ID = "fee4_id";
  public static final String FEE6_ID = "fee6_id";
  public static final String PAYMENT1_ID = "payment1_id";
  public static final String PAYMENT2_ID = "payment2_id";
  public static final String PAYMENT4_ID = "payment4_id";

  public static final String COURSE1_ID = "course1_id";

  public static final String COURSE2_ID = "course2_id";

  public static final String COURSE3_ID = "course3_id";

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
    when(cognitoComponent.getEmailByIdToken(BAD_TOKEN)).thenReturn(null);
    when(cognitoComponent.getEmailByIdToken(STUDENT1_TOKEN)).thenReturn("test+ryan@hei.school");
    when(cognitoComponent.getEmailByIdToken(TEACHER1_TOKEN)).thenReturn("test+teacher1@hei.school");
    when(cognitoComponent.getEmailByIdToken(MANAGER1_TOKEN)).thenReturn("test+manager1@hei.school");
  }

  public static void setUpEventBridge(EventBridgeClient eventBridgeClient) {
    when(eventBridgeClient.putEvents((PutEventsRequest) any())).thenReturn(
        PutEventsResponse.builder().build()
    );
  }

  public static void assertThrowsApiException(String expectedBody, Executable executable) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    assertEquals(expectedBody, apiException.getResponseBody());
  }

  public static void assertThrowsForbiddenException(Executable executable) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals("{"
        + "\"type\":\"403 FORBIDDEN\","
        + "\"message\":\"Access is denied\"}", responseBody);
  }

  public static boolean isValidUUID(String candidate) {
    try {
      UUID.fromString(candidate);
      return true;
    } catch (Exception e) {
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
