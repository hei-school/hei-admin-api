package school.hei.haapi.endpoint.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping("/ping")
  public String hello() {
    return "pong";
  }
}
