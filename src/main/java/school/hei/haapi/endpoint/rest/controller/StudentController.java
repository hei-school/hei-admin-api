package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import java.util.PrimitiveIterator;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.AwardedCourseMapper;
import school.hei.haapi.endpoint.rest.mapper.GroupFlowMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.CreateGroupFlow;
import school.hei.haapi.endpoint.rest.model.GroupFlow;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.GroupFlowService;
import school.hei.haapi.service.UserService;

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.model.User.Role.STUDENT;

@RestController
@AllArgsConstructor
public class StudentController {
  private final UserService userService;
  private final UserMapper userMapper;
  private final GroupFlowService groupFlowService;
  private final GroupFlowMapper groupFlowMapper;
  @GetMapping("/students/{id}")
  public Student getStudentById(@PathVariable String id) {
    return userMapper.toRestStudent(userService.getById(id));
  }

  //todo: to review
  @GetMapping("/groups/{groupId}/students")
  public List<Student> getStudentByGroupId(@PathVariable String groupId) {
    return userService.getByGroupId(groupId).stream()
        .map(userMapper::toRestStudent)
        .collect(Collectors.toList());
  }
  //todo: get with course
  @GetMapping("/students")
  public List<Student> getStudents(
      @RequestParam PageFromOne page, @RequestParam("page_size") BoundedPageSize pageSize,
      @RequestParam(value = "ref", required = false, defaultValue = "") String ref,
      @RequestParam(value = "first_name", required = false, defaultValue = "") String firstName,
      @RequestParam(value = "last_name", required = false, defaultValue = "") String lastName,
      @RequestParam(value = "course_id", required = false, defaultValue = "") String courseId) {
    return userService.getByLinkedCourse(STUDENT, firstName, lastName, ref, courseId,  page, pageSize).stream()
        .map(userMapper::toRestStudent)
        .collect(toUnmodifiableList());
  }

  @PutMapping("/students")
  public List<Student> saveAll(@RequestBody List<Student> toWrite) {
    return userService
        .saveAll(toWrite.stream()
            .map(userMapper::toDomain)
            .collect(toUnmodifiableList())).stream()
        .map(userMapper::toRestStudent)
        .collect(toUnmodifiableList());
  }

  @PostMapping("/students/{id}/group_flows")
  public GroupFlow saveStudentGroup(
      @PathVariable String id,
      @RequestBody CreateGroupFlow createGroupFlow
  ) {
    return groupFlowMapper.toRest(groupFlowService.save(createGroupFlow));
  }

}