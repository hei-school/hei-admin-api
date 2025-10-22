package school.hei.haapi.endpoint.rest.controller;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.AttendanceRestMapper;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.StudentGlobalAttendance;
import school.hei.haapi.service.AttendanceService;

@RestController
@AllArgsConstructor
public class AttendanceController {
  private final AttendanceService attendanceService;
  private final AttendanceRestMapper attendanceRestMapper;

  @GetMapping("/students/{id}/attendance")
  public List<StudentGlobalAttendance> getStudentAttendance(
      @PathVariable("id") String studentId,
      @RequestParam Instant from,
      @RequestParam Instant to,
      @RequestParam AttendanceStatus attendanceStatus,
      @RequestParam("title") List<String> titles) {
    var studentAttendances =
        attendanceService.getStudentAttendanceByStudentId(
            studentId, attendanceStatus, from, to, titles);
    return studentAttendances.stream().map(attendanceRestMapper::toRest).toList();
  }
}
