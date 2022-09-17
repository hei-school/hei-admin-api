package school.hei.haapi.endpoint.rest.controller;

import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.model.Whoami;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class WhoamiController {
  private final UserService userService;

  @GetMapping("/whoami")
  public Whoami hello(@AuthenticationPrincipal Principal principal) {
    Whoami whoami = new Whoami();
    whoami.setId(principal.getUserId());
    whoami.setBearer(principal.getBearer());
    whoami.setRole(Whoami.RoleEnum.valueOf(principal.getRole()));
    return whoami;
  }

  //accepts JSON for SECURITYIT
  @PostMapping(path = "/whoamiface",
      consumes = {
      MediaType.IMAGE_PNG_VALUE,
          MediaType.IMAGE_JPEG_VALUE,
          MediaType.APPLICATION_JSON_VALUE
  }
  )
  public Whoami whoamiface(
      @RequestBody byte[] toCompare,
      @AuthenticationPrincipal Principal principal
  ){
    var whoIam = userService.rekognisePicture(toCompare);
    Whoami whoami = new Whoami();
    whoami.setId(whoIam.getId());
    whoami.setBearer(principal.getBearer());
    whoami.setRole(Whoami.RoleEnum.valueOf(whoIam.getRole().name()));
    return whoami;
  }
}
