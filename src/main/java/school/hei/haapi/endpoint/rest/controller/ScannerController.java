package school.hei.haapi.endpoint.rest.controller;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Scanner;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.UserService;

import java.util.List;

@RestController
@AllArgsConstructor
public class ScannerController {
  private final UserService userService;
  private final UserRepository userRepository;
  private final UserMapper mapper;

  @GetMapping("/scanners")
  public List<Scanner> getScannerUsers(
      @RequestParam PageFromOne page,
      @RequestParam("page_size") BoundedPageSize pageSize,
      @RequestParam(value = "ref", required = false, defaultValue = "") String ref,
      @RequestParam(value = "first_name", required = false, defaultValue = "") String firstName,
      @RequestParam(value = "last_name", required = false, defaultValue = "") String lastName
  ) {
    return userService
        .getByCriteria(User.Role.SCANNER, firstName, lastName, ref, page, pageSize)
        .stream()
        .map(mapper::toRestScannerUser)
        .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping("/scanners/{id}")
  public Scanner getScannerUserById(@PathVariable(name = "id")String id) {
    return mapper.toRestScannerUser(userService.getById(id));
  }

  @PutMapping("/scanners")
  public List<Scanner> createOrUpdateScanerUsers(@RequestBody List<Scanner> toCreateOrUpdate) {
    return userService.saveAll(toCreateOrUpdate.stream()
        .map(mapper::toDomain)
        .collect(Collectors.toUnmodifiableList()))
        .stream()
        .map(mapper::toRestScannerUser)
        .collect(Collectors.toUnmodifiableList());
  }
}
