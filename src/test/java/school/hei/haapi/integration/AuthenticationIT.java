package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.ClientUtils;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = AuthenticationIT.ContextInitializer.class)
@AutoConfigureMockMvc
class AuthenticationIT {

  static class ContextInitializer extends AbstractContextInitializer {
    @Override
    public int getServerPort() {
      return 8080;
    }
  }

  @Autowired private CognitoComponent cognitoComponent;

  @Test
  @Disabled("no test Cognito")
  void user_is_authenticated() {
    String bearer = "TODO: get bearer by really authenticating against Cognito";

    String email = cognitoComponent.findEmailByBearer(bearer);

    assertEquals("ryan@hei.school", email);
  }

  @Test
  void user_is_unauthenticated() {
    String bearer = ClientUtils.BAD_TOKEN; // null, invalid or expired

    assertThrows(
        ForbiddenException.class,
        () -> cognitoComponent.findEmailByBearer(bearer));
  }
}
