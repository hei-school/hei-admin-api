package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.integration.conf.AuthenticationContextInitializer;
import school.hei.haapi.integration.conf.CallerData;
import school.hei.haapi.security.cognito.CognitoComponent;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = AuthenticationContextInitializer.class)
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

  @Autowired private CognitoComponent cognitoComponent;
  @Autowired private CallerData callerData;

  @Test
  void user_is_authenticated() {
    String bearer = callerData.getToken();

    String email = cognitoComponent.findEmailByBearer(bearer);

    assertEquals("ryan@hei.school", email);
  }

  @Test
  void user_is_unauthenticated() {
    String bearer = CallerData.BAD_TOKEN; // null, invalid or expired

    assertThrows(
        ForbiddenException.class,
        () -> {
          cognitoComponent.findEmailByBearer(bearer);
        });
  }
}
