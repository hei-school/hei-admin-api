package school.hei.haapi.endpoint.rest.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.model.Whoami;
import school.hei.haapi.endpoint.rest.security.model.Principal;

@RestController
public class WhoamiController {

  @GetMapping("/whoami")
  public Whoami hello(@AuthenticationPrincipal Principal principal) {
    Whoami whoami = new Whoami();
    whoami.setId(principal.getUserId());
    whoami.setBearer(principal.getBearer());
    whoami.setRole(Whoami.RoleEnum.valueOf(principal.getRole()));
    return whoami;
  }
}
