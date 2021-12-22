package school.hei.haapi.endpoint.rest.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.model.Whoami;
import school.hei.haapi.endpoint.rest.security.model.ApiClient;

@RestController
public class WhoamiController {

  @GetMapping("/whoami")
  public Whoami hello(@AuthenticationPrincipal ApiClient client) {
    Whoami whoami = new Whoami();
    whoami.setId(client.getUserId());
    whoami.setRole(Whoami.RoleEnum.valueOf(client.getRole()));
    return whoami;
  }
}
