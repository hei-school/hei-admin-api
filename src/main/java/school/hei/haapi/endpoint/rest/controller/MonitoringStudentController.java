package school.hei.haapi.endpoint.rest.controller;

import static java.util.UUID.randomUUID;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.LinkStudentsByMonitorIdRequest;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.service.MonitoringStudentService;

@RestController
@AllArgsConstructor
public class MonitoringStudentController {
  private final MonitoringStudentService monitoringStudentService;
  private final UserMapper userMapper;

  @PutMapping(value = "/monitors/{id}/students")
  public List<Student> linkStudentsByMonitorId(
      @PathVariable String id, @RequestBody LinkStudentsByMonitorIdRequest request) {
    return monitoringStudentService
        .linkMonitorFollowingStudents(id, request.getStudentsIds())
        .stream()
        .map(userMapper::toRestStudent)
        .toList();
  }

  @GetMapping(value = "/monitors/{id}/students")
  public List<Student> getLinkedStudentsByMonitorId(
      @PathVariable String id,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return monitoringStudentService.getStudentsByMonitorId(id, page, pageSize).stream()
        .map(userMapper::toRestStudent)
        .toList();
  }

  @GetMapping("/monitors/{monitor_id}/students/{student_id}")
  public Student getLinkedStudentByIdAndMonitorId(
      @PathVariable(name = "monitor_id") String monitorId,
      @PathVariable(name = "student_id") String studentId) {
    return userMapper.toRestStudent(
        User.builder()
            .id(randomUUID().toString())
            .firstName("Axel")
            .lastName("HEI")
            .email("something-unique@mail.hei.school")
            .ref("STD" + randomUUID())
            .phone("+261 34 94 543 21")
            .address("123 Avenue de l'Indépendance")
            .role(User.Role.STUDENT)
            .status(User.Status.ENABLED)
            .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
            .groupFlows(new ArrayList<>())
            .build());
  }
}
