package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.AttendanceMapper;
import school.hei.haapi.endpoint.rest.model.CreateAttendanceMovement;
import school.hei.haapi.endpoint.rest.model.StudentAttendanceMovement;
import school.hei.haapi.service.AttendanceService;

@RestController
@AllArgsConstructor
public class AttendanceController {
  private final AttendanceService attendanceService;
  private final AttendanceMapper attendanceMapper;

  @PostMapping("/attendance/movement")
  public StudentAttendanceMovement createAttendanceMovement (@RequestBody CreateAttendanceMovement movement) {
    return attendanceMapper.toRestMovement(
        attendanceService.createStudentAttendanceMovement(attendanceMapper.toDomain(movement)
        ));
  }
}
