package school.hei.haapi.integration.conf;

import static java.util.UUID.randomUUID;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.List;
import org.casbin.casdoor.entity.CasdoorRole;
import org.casbin.casdoor.entity.CasdoorUser;
import org.casbin.casdoor.service.CasdoorAuthService;
import school.hei.haapi.endpoint.rest.security.casdoorAuthentication.config.CertificateLoader;
import school.hei.haapi.model.User;

/**
 * Mints bearer tokens for users a test owns.
 *
 * <p>CasdoorAuthProvider parses the bearer into a CasdoorUser, checks its role against the test
 * organization, then loads the application user <em>by email</em>. So a token is nothing more than
 * a key into the mock, and any user a test has persisted can be authenticated as itself — no seeded
 * row, no shared constant.
 */
public class TestAuth {
  /** Matches CASDOOR_ORGANIZATION_NAME as set for the tests. */
  private static final String TEST_ORGANIZATION = "dummy";

  public static void setUpCertificate(CertificateLoader certificateLoader) {
    given(certificateLoader.getCertificate()).willReturn("mocked-certificate");
  }

  /**
   * Returns a bearer authenticating as {@code user}, whose role is taken from the user itself.
   *
   * <p>The user must already be persisted: authentication resolves it by email.
   */
  public static String tokenFor(CasdoorAuthService casdoorAuthService, User user) {
    return tokenFor(casdoorAuthService, user.getEmail(), user.getRole());
  }

  public static String tokenFor(
      CasdoorAuthService casdoorAuthService, String email, User.Role role) {
    var token = randomUUID().toString();
    when(casdoorAuthService.parseJwtToken(token)).thenReturn(casdoorUser(email, role));
    return token;
  }

  private static CasdoorUser casdoorUser(String email, User.Role role) {
    var casdoorRole = new CasdoorRole();
    casdoorRole.setOwner(TEST_ORGANIZATION);
    casdoorRole.setName(role.name().toLowerCase());
    casdoorRole.setUsers(new String[] {TEST_ORGANIZATION + "/user"});

    var casdoorUser = new CasdoorUser();
    casdoorUser.setEmail(email);
    casdoorUser.setRoles(List.of(casdoorRole));
    return casdoorUser;
  }
}
