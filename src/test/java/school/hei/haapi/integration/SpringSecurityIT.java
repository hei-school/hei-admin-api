package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PUT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;

@Testcontainers
class SpringSecurityIT extends FacadeIT {
  @LocalServerPort private int serverPort;

  @MockBean private SentryConf sentryConf;

  @Autowired private CognitoComponent cognitoComponent;

  @Value("${test.aws.cognito.idToken}")
  private String bearer;

  @Disabled("Cognito should be mocked")
  @Test
  void authenticated_user_has_known_email() {
    System.out.println("------------------------------test+-------------------");
    String email = cognitoComponent.getEmailByIdToken(bearer);
    assertEquals("test+ryan@hei.school", email);
  }

  @Test
  void unauthenticated_user_is_forbidden() {
    assertNull(cognitoComponent.getEmailByIdToken(BAD_TOKEN));
  }

  @Test
  void ping_with_cors() throws IOException, InterruptedException {
    // /!\ The HttpClient produced by openapi-generator SEEMS to not support text/plain
    HttpClient unauthenticatedClient = HttpClient.newBuilder().build();
    String basePath = "http://localhost:" + serverPort;

    HttpResponse<String> response =
        unauthenticatedClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(basePath + "/ping"))
                // cors
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://localhost:3000")
                .build(),
            HttpResponse.BodyHandlers.ofString());

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertEquals("pong", response.body());
    // cors
    var headers = response.headers();
    var origins = headers.allValues("Access-Control-Allow-Origin");
    assertEquals(1, origins.size());
    assertEquals("*", origins.get(0));
  }

  @Test
  void options_has_cors_headers() throws IOException, InterruptedException {
    test_cors(GET, "/whoami");
    test_cors(PUT, "/students");
  }

  void test_cors(HttpMethod method, String path) throws IOException, InterruptedException {
    HttpClient unauthenticatedClient = HttpClient.newBuilder().build();
    String basePath = "http://localhost:" + serverPort;

    HttpResponse<String> response =
        unauthenticatedClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create(basePath + path))
                .method(OPTIONS.name(), HttpRequest.BodyPublishers.noBody())
                .header("Access-Control-Request-Headers", "authorization")
                .header("Access-Control-Request-Method", method.name())
                .header("Origin", "http://localhost:3000")
                .build(),
            HttpResponse.BodyHandlers.ofString());

    var headers = response.headers();
    var origins = headers.allValues("Access-Control-Allow-Origin");
    assertEquals(1, origins.size());
    assertEquals("*", origins.get(0));
    var headersList = headers.allValues("Access-Control-Allow-Headers");
    assertEquals(1, headersList.size());
    assertEquals("authorization", headersList.get(0));
  }

  // TODO: For instance, we set the timezone to be UTC+3 through jackson-time-zone
  // and verify if it's really the case when the app is running
  @Test
  void check_timezone_is_utc_plus_three() {
    ZoneId zoneId = ZoneId.of("Indian/Antananarivo");
    String utc = "+03:00";
    ZoneOffset offset = zoneId.getRules().getOffset(Instant.now());
    assertEquals(utc, offset.getId());
  }
}
