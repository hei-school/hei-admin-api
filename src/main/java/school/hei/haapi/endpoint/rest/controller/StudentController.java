package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static school.hei.haapi.model.User.Role.STUDENT;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.GroupFlowMapper;
import school.hei.haapi.endpoint.rest.mapper.SexEnumMapper;
import school.hei.haapi.endpoint.rest.mapper.StatusEnumMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.CreateGroupFlow;
import school.hei.haapi.endpoint.rest.model.CrupdateStudent;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.GroupFlow;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.service.GroupFlowService;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.aws.FileService;

@RestController
@AllArgsConstructor
public class StudentController {
  private final UserService userService;
  private final UserMapper userMapper;
  private final GroupFlowService groupFlowService;
  private final GroupFlowMapper groupFlowMapper;
  private final StatusEnumMapper statusEnumMapper;
  private final SexEnumMapper sexEnumMapper;
  private final FileService fileService;

  @PostMapping(value = "/students/{id}/picture/raw", consumes = IMAGE_PNG_VALUE)
  public Student uploadStudentProfilePicture(
      @RequestBody byte[] profilePicture, @PathVariable(name = "id") String studentId) {
    File tempFile = fileService.createTempFile(profilePicture);
    userService.uploadUserProfilePicture(tempFile, studentId);
    return userMapper.toRestStudent(userService.findById(studentId));
  }

  @GetMapping("/students/{id}")
  public Student getStudentById(@PathVariable String id) {
    return userMapper.toRestStudent(userService.findById(id));
  }

  // todo: to review
  @GetMapping("/groups/{groupId}/students")
  public List<Student> getStudentByGroupId(@PathVariable String groupId) {
    return userService.getByGroupId(groupId).stream()
        .map(userMapper::toRestStudent)
        .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping("/students")
  public List<Student> getStudents(
      @RequestParam PageFromOne page,
      @RequestParam("page_size") BoundedPageSize pageSize,
      @RequestParam(value = "ref", required = false, defaultValue = "") String ref,
      @RequestParam(value = "first_name", required = false, defaultValue = "") String firstName,
      @RequestParam(value = "last_name", required = false, defaultValue = "") String lastName,
      @RequestParam(value = "course_id", required = false, defaultValue = "") String courseId,
      @RequestParam(name = "status", required = false) EnableStatus status,
      @RequestParam(name = "sex", required = false) Sex sex) {
    User.Sex domainSex = sexEnumMapper.toDomainSexEnum(sex);
    User.Status domainStatus = statusEnumMapper.toDomainStatus(status);
    return userService
        .getByLinkedCourse(
            STUDENT, firstName, lastName, ref, courseId, page, pageSize, domainStatus, domainSex)
        .stream()
        .map(userMapper::toRestStudent)
        .collect(toUnmodifiableList());
  }

  @PutMapping("/students")
  public List<Student> saveAll(@RequestBody List<CrupdateStudent> toWrite) {
    return userService
        .saveAll(toWrite.stream().map(userMapper::toDomain).collect(toUnmodifiableList()))
        .stream()
        .map(userMapper::toRestStudent)
        .collect(toUnmodifiableList());
  }

  @PutMapping("/students/{id}")
  public Student updateStudent(
      @PathVariable(name = "id") String studentId, @RequestBody CrupdateStudent toUpdate) {
    return userMapper.toRestStudent(
        userService.updateUser(userMapper.toDomain(toUpdate), studentId));
  }

  @PostMapping("/students/{id}/group_flows")
  public GroupFlow saveStudentGroup(
      @PathVariable String id, @RequestBody CreateGroupFlow createGroupFlow) {
    return groupFlowMapper.toRest(groupFlowService.save(createGroupFlow));
  }
}