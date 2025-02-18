package school.hei.haapi.endpoint.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.casbin.casdoor.exception.CasdoorAuthException;
import org.casbin.casdoor.service.CasdoorAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.security.casdoorAuthentication.model.Result;
import school.hei.haapi.endpoint.rest.security.model.Principal;

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
  public Result getRedirectUrl() {
    try {
      String signinUrl = casdoorAuthService.getSigninUrl(redirectUrl);
      return Result.success(signinUrl);
    } catch (CasdoorAuthException exception) {
      log.error("casdoor auth exception", exception);
      return Result.failure(exception.getMessage());
    }
  }

  @PostMapping("/authentication/signin")
  public Result signin(@RequestParam("code") String code, @RequestParam("state") String state) {
    try {
      String token = casdoorAuthService.getOAuthToken(code, state);
      return Result.success(token);
    } catch (CasdoorAuthException exception) {
      log.error("casdoor auth exception", exception);
      return Result.failure(exception.getMessage());
    }
  }

  @GetMapping("/authentication/userinfo")
  public Result userinfo(@AuthenticationPrincipal Principal principal) {
    return Result.success(principal);
  }
}
