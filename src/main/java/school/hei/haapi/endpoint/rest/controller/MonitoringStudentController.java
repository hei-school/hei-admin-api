package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.MonitoringStudentService;

@RestController
@AllArgsConstructor
public class MonitoringStudentController {
  private final MonitoringStudentService monitoringStudentService;
  private final UserMapper userMapper;

  @PutMapping(value = "/monitors/{id}/students")
  public List<Student> linkStudentsByMonitorId(
      @PathVariable String id, @RequestBody List<UserIdentifier> studentsIdentifier) {
    return monitoringStudentService
        .linkMonitorFollowingStudents(
            id, studentsIdentifier.stream().map(userMapper::toDomain).collect(toUnmodifiableList()))
        .stream()
        .map(userMapper::toRestStudent)
        .collect(toUnmodifiableList());
  }

  @GetMapping(value = "/monitors/{id}/students")
  public List<Student> getLinkedStudentsByMonitorId(
      @PathVariable String id,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return monitoringStudentService.getStudentsByMonitorId(id, page, pageSize).stream()
        .map(userMapper::toRestStudent)
        .collect(toUnmodifiableList());
  }
}
