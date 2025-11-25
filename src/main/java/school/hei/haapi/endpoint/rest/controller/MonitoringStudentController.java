package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import school.hei.haapi.endpoint.rest.mapper.MonitorStudentLinkMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.LinkStudentsByMonitorIdRequest;
import school.hei.haapi.endpoint.rest.model.MonitorStudentLink;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.UpdateMonitorStudentLinkStatusRequest;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.dto.MonitorStudentLinkDto;
import school.hei.haapi.service.MonitoringStudentService;

@RestController
@AllArgsConstructor
public class MonitoringStudentController {
  private final MonitoringStudentService monitoringStudentService;
  private final UserMapper userMapper;
  private final MonitorStudentLinkMapper monitorStudentLinkMapper;

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

  @GetMapping("/monitors/link")
  public List<MonitorStudentLink> getLinkStudentRequests(
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return monitoringStudentService.getLinkStudentRequests(page, pageSize).stream()
        .map(monitorStudentLinkMapper::toRest)
        .toList();
  }

  @PostMapping("/monitors/link")
  public List<MonitorStudentLink> updateMonitorStudentLinkStatus(
      @RequestBody UpdateMonitorStudentLinkStatusRequest request) {
    List<MonitorStudentLinkDto> monitorStudentLinkUpdated =
        monitoringStudentService.approveLinkStudentMonitor(
            request.getMonitorStudentLink().stream().map(monitorStudentLinkMapper::toDto).toList());
    return monitorStudentLinkUpdated.stream().map(monitorStudentLinkMapper::toRest).toList();
  }

  @GetMapping("/monitors/{monitor_id}/students/{student_id}")
  public Student getLinkedStudentByIdAndMonitorId(
      @PathVariable(name = "monitor_id") String monitorId,
      @PathVariable(name = "student_id") String studentId) {
    return userMapper.toRestStudent(
        monitoringStudentService.getStudentByIdAndMonitorId(studentId, monitorId));
  }
}
