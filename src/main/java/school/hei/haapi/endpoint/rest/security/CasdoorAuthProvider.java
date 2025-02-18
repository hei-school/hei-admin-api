package school.hei.haapi.endpoint.rest.security;

import static java.util.Optional.empty;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.casbin.casdoor.entity.CasdoorUser;
import org.casbin.casdoor.exception.CasdoorAuthException;
import org.casbin.casdoor.service.CasdoorAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.security.casdoorAuthentication.model.CustomUserDetails;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.UserService;

@Component
@Slf4j
public class CasdoorAuthProvider extends AbstractUserDetailsAuthenticationProvider {
  private static final String BEARER_PREFIX = "Bearer ";
  private final UserService userService;
  private final CasdoorAuthService casdoorAuthService;
  private final String casdoorOrganizationName;
  private static final Map<String, User.Role> ROLE_MAP =
      Arrays.stream(User.Role.values())
          .collect(Collectors.toMap(role -> role.name().toLowerCase(), role -> role));

  public CasdoorAuthProvider(
      UserService userService,
      CasdoorAuthService casdoorAuthService,
      @Value("${CASDOOR_ORGANIZATION_NAME}") String casdoorOrganizationName) {
    this.userService = userService;
    this.casdoorAuthService = casdoorAuthService;
    this.casdoorOrganizationName = casdoorOrganizationName;
  }

  @Override
  protected void additionalAuthenticationChecks(
      UserDetails userDetails, UsernamePasswordAuthenticationToken token) {
    // nothing
  }

  @Override
  protected UserDetails retrieveUser(
      String username, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
    String bearer =
        getBearer(usernamePasswordAuthenticationToken)
            .orElseThrow(() -> new UsernameNotFoundException("Bad credentials"));

    CasdoorUser casdoorUser = null;
    try {
      casdoorUser = casdoorAuthService.parseJwtToken(bearer);
    } catch (CasdoorAuthException exception) {
      log.error("casdoor auth exception", exception);
      throw new UsernameNotFoundException("Bad credentials");
    }
    boolean hasRole =
        casdoorUser.getRoles().stream()
            .anyMatch(
                role ->
                    ROLE_MAP.containsKey(role.getName().toLowerCase())
                        && role.getOwner().equals(casdoorOrganizationName));
    if (!hasRole) {
      String email = casdoorUser.getEmail();
      log.error("Casdoor auth exception: User with email {} doesn't have the correct role", email);
      throw new UsernameNotFoundException("Bad credentials");
    }
    try {
      return new Principal(userService.getByEmail(casdoorUser.getEmail()), bearer);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new UsernameNotFoundException("Bad credentials");
    }
  }

  private static Optional<String> getBearer(
      UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
    Object tokenObject = usernamePasswordAuthenticationToken.getCredentials();
    if (!(tokenObject instanceof String) || !((String) tokenObject).startsWith(BEARER_PREFIX)) {
      return empty();
    }
    return Optional.of(((String) tokenObject).substring(BEARER_PREFIX.length()).trim());
  }

  public static CustomUserDetails getPrincipal() {
    SecurityContext context = SecurityContextHolder.getContext();
    Authentication authentication = context.getAuthentication();
    return (CustomUserDetails) authentication.getPrincipal();
  }
}
