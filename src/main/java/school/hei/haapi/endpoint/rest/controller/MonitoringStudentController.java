package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;
import school.hei.haapi.service.MonitoringStudentService;

@RestController
@AllArgsConstructor
public class MonitoringController {
  private final MonitoringStudentService monitoringStudentService;
  private final UserMapper userMapper;

  @PutMapping(value = "/monitors/{id}/students")
  public List<Student> linkStudentsByMonitorId(
      @PathVariable String id, @RequestBody List<UserIdentifier> studentsIdentifier) {
    return monitoringStudentService
        .linkMonitorFollowingStudents(
            id,
            studentsIdentifier.stream()
                .map(userMapper::IdentifierToUser)
                .collect(toUnmodifiableList()))
        .stream()
        .map(userMapper::toRestStudent)
        .collect(toUnmodifiableList());
  }
}
