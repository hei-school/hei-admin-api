package school.hei.haapi.endpoint.rest.controller;

import static org.apache.oltu.oauth2.common.error.OAuthError.TokenResponse.INVALID_GRANT;

import lombok.extern.slf4j.Slf4j;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.casbin.casdoor.exception.CasdoorAuthException;
import org.casbin.casdoor.service.CasdoorAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.exception.BadRequestException;

@RestController
@Slf4j
public class AuthenticationController {
  private final CasdoorAuthService casdoorAuthService;
  private final String redirectUrl;

  public AuthenticationController(
      CasdoorAuthService casdoorAuthService, @Value("${casdoor.redirect-url}") String redirectUrl) {
    this.casdoorAuthService = casdoorAuthService;
    this.redirectUrl = redirectUrl;
  }

  @GetMapping("/authentication/login-url")
  public String getRedirectUrl(
      @RequestParam(value = "redirect_uri", required = false) String givenURL) {
    if (givenURL != null && !givenURL.isEmpty()) {
      return casdoorAuthService.getSigninUrl(givenURL);
    }
    return casdoorAuthService.getSigninUrl(redirectUrl);
  }

  @PostMapping("/authentication/signin")
  public String signin(@RequestParam("code") String code, @RequestParam("state") String state) {
    try {
      return casdoorAuthService.getOAuthToken(code, state);
    } catch (CasdoorAuthException e) {
      throw rethrowAsBadRequestIfCodeIsSpent(e);
    }
  }

  private RuntimeException rethrowAsBadRequestIfCodeIsSpent(CasdoorAuthException e) {
    if (e.getCause() instanceof OAuthProblemException problem
        && INVALID_GRANT.equals(problem.getError())) {
      log.info("Casdoor refused authorization code: {}", problem.getDescription());
      throw new BadRequestException("Authorization code is invalid or has already been used");
    }
    return e;
  }
}
