package school.hei.haapi.endpoint.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.casbin.casdoor.service.CasdoorAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
  public String getRedirectUrl() {
    return casdoorAuthService.getSigninUrl(redirectUrl);
  }

  @PostMapping("/authentication/signin")
  public String signin(@RequestParam("code") String code, @RequestParam("state") String state) {
    return casdoorAuthService.getOAuthToken(code, state);
  }
}
