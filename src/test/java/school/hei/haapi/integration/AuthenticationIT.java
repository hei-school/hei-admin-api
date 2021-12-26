package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = AuthenticationIT.ContextInitializer.class)
@AutoConfigureMockMvc
class AuthenticationIT {

  public static class ContextInitializer extends AbstractContextInitializer {
    @Override
    public int getServerPort() {
      return anAvailableRandomPort();
    }
  }

  @Autowired private CognitoComponent cognitoComponent;
  @Value("${test.cognito.idToken}") private String bearer;

  @Test
  void authenticated_user_has_known_email() {
    String email = cognitoComponent.getEmailByBearer(bearer);
    assertEquals("lou@hei.school", email);
  }

  @Test
  void unauthenticated_user_is_forbidden() {
    assertNull(cognitoComponent.getEmailByBearer(BAD_TOKEN));
  }
}
