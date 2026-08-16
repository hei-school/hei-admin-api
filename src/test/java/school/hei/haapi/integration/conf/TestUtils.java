package school.hei.haapi.integration.conf;

import java.io.IOException;
import java.net.ServerSocket;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.model.GroupIdentifier;

/**
 * Connection concerns only: how a test reaches the API and what an unusable token or id looks like.
 *
 * <p>Test data belongs to the test that needs it — see the builders under {@code
 * school.hei.haapi.integration.testData} — and tokens are minted per test by {@link TestAuth}.
 */
public class TestUtils {
  public static final String BAD_TOKEN = "bad_token";
  public static final String NOT_EXISTING_ID = "not_existing_id";

  public static ApiClient anApiClient(String token, int serverPort) {
    var client = new ApiClient();
    client.setScheme("http");
    client.setHost("localhost");
    client.setPort(serverPort);
    if (token != null)
      client.setRequestInterceptor(
          httpRequestBuilder -> httpRequestBuilder.header("Authorization", "Bearer " + token));
    return client;
  }

  public static int anAvailableRandomPort() {
    try {
      return new ServerSocket(0).getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static GroupIdentifier createGroupIdentifier(Group group) {
    return new GroupIdentifier().ref(group.getRef()).name(group.getName()).id(group.getId());
  }
}
