package school.hei.haapi.integration.conf;

import school.hei.haapi.endpoint.rest.client.ApiClient;

public class ClientUtils {
  public static final String STUDENT1_ID = "student1_id";
  public static final String STUDENT2_ID = "student2_id";
  public static final String TEACHER1_ID = "teacher1_id";
  public static final String TEACHER2_ID = "teacher2_id";
  public static final String BAD_TOKEN = "bad_token"; // null, invalid or expired
  public static final String STUDENT1_TOKEN = "student1_token";
  public static final String TEACHER1_TOKEN = "teacher1_token";
  public static final String GROUP1_ID = "group1_id";

  public static ApiClient aClient(String token, int serverPort) {
    ApiClient client = new ApiClient();
    client.setScheme("http");
    client.setHost("localhost");
    client.setPort(serverPort);
    client.setRequestInterceptor(httpRequestBuilder ->
        httpRequestBuilder.header("Authorization", "Bearer " + token));
    return client;
  }
}
