package school.hei.haapi.endpoint.rest.controller;


import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.AttendanceMapper;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.CreateAttendanceMovement;
import school.hei.haapi.endpoint.rest.model.StudentAttendance;
import school.hei.haapi.endpoint.rest.model.StudentAttendanceMovement;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
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

  @GetMapping("/attendance")
  public List<StudentAttendance> getStudentsAttendance(
      @RequestParam(name = "attendance_statuses")List<AttendanceStatus> attendanceStatuses,
      @RequestParam(name = "courses_ids")List<String> coursesIds,
      @RequestParam(name = "student_key_word")String studentKeyWord,
      @RequestParam(name = "from")Instant from,
      @RequestParam(name = "to")Instant to,
      @RequestParam(name = "page")PageFromOne page, @RequestParam(name = "page_size")BoundedPageSize
      ) {
    return
  }
}
