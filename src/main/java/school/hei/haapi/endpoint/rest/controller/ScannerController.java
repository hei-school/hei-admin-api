package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class ScannerController {
  private final UserService userService;
  private final UserRepository userRepository;
}
