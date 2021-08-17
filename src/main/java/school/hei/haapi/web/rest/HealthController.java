package school.hei.haapi.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping("/ping")
  public String hello() {
    return "pong";
  }
}
