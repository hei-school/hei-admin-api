package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PUT;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.testData.StudentTestData.axel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;

@Testcontainers
@AutoConfigureMockMvc
class SpringSecurityIT extends FacadeITMockedThirdParties {
  @BeforeEach
  public void setUp() {
    setUpS3Service(fileService, axel());
  }

  @Test
  void unauthenticated_user_is_forbidden() {
    assertNull(cognitoComponentMock.getEmailByIdToken(BAD_TOKEN));
  }

  @Test
  void ping_with_cors() throws IOException, InterruptedException {
    // /!\ The HttpClient produced by openapi-generator SEEMS to not support text/plain
    var unauthenticatedClient = HttpClient.newBuilder().build();
    var basePath = "http://localhost:" + localPort;

    var response =
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
    assertEquals("*", origins.getFirst());
  }

  @Test
  void options_has_cors_headers() throws IOException, InterruptedException {
    test_cors(GET, "/whoami");
    test_cors(PUT, "/students");
  }

  /** Helper of {@link #options_has_cors_headers()}, not a test of its own. */
  private void test_cors(HttpMethod method, String path) throws IOException, InterruptedException {
    var unauthenticatedClient = HttpClient.newBuilder().build();
    var basePath = "http://localhost:" + localPort;

    var response =
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
    assertEquals("*", origins.getFirst());
    var headersList = headers.allValues("Access-Control-Allow-Headers");
    assertEquals(1, headersList.size());
    assertEquals("authorization", headersList.getFirst());
  }

  // TODO: For instance, we set the timezone to be UTC+3 through jackson-time-zone
  // and verify if it's really the case when the app is running
  @Test
  void check_timezone_is_utc_plus_three() {
    var zoneId = ZoneId.of("Indian/Antananarivo");
    var utc = "+03:00";
    var offset = zoneId.getRules().getOffset(Instant.now());
    assertEquals(utc, offset.getId());
  }
}
