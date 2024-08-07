package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.SexEnumMapper;
import school.hei.haapi.endpoint.rest.mapper.StatusEnumMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateManager;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Manager;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.validator.CoordinatesValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.aws.FileService;

@RestController
@AllArgsConstructor
public class ManagerController {
  private final SexEnumMapper sexEnumMapper;
  private final StatusEnumMapper statusEnumMapper;
  private final UserService userService;
  private final UserMapper userMapper;
  private final FileService fileService;
  private final CoordinatesValidator validator;

  @PostMapping(value = "/managers/{id}/picture/raw", consumes = MULTIPART_FORM_DATA_VALUE)
  public Manager uploadTeacherProfilePicture(
      @RequestPart("file_to_upload") MultipartFile profilePictureAsMultipartFile,
      @PathVariable String id) {
    userService.uploadUserProfilePicture(profilePictureAsMultipartFile, id);
    return userMapper.toRestManager(userService.findById(id));
  }

  @GetMapping(value = "/managers/{id}")
  public Manager getManagerById(@PathVariable String id) {
    return userMapper.toRestManager(userService.findById(id));
  }

  @PutMapping("/managers/{id}")
  public Manager updateManager(
      @PathVariable(name = "id") String managerId, @RequestBody CrupdateManager toUpdate) {
    validator.accept(toUpdate.getCoordinates());
    return userMapper.toRestManager(
        userService.updateUser(userMapper.toDomain(toUpdate), managerId));
  }

  @GetMapping(value = "/managers")
  public List<Manager> getManagers(
      @RequestParam PageFromOne page,
      @RequestParam("page_size") BoundedPageSize pageSize,
      @RequestParam(name = "status", required = false) EnableStatus status,
      @RequestParam(name = "sex", required = false) Sex sex) {
    User.Sex domainSex = sexEnumMapper.toDomainSexEnum(sex);
    User.Status domainStatus = statusEnumMapper.toDomainStatus(status);
    return userService
        .getByRole(User.Role.MANAGER, page, pageSize, domainStatus, domainSex)
        .stream()
        .map(userMapper::toRestManager)
        .collect(toUnmodifiableList());
  }
}
