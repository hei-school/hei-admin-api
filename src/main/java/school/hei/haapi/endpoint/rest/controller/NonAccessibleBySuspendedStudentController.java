package school.hei.haapi.endpoint.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NonAccessibleBySuspendedStudentController {
  @GetMapping("/non-accessible-by-suspended")
  public String amISuspended() {
    return "not suspended";
  }
}
