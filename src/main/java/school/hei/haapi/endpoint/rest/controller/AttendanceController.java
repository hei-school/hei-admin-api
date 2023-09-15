package school.hei.haapi.endpoint.rest.controller;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
      @RequestParam(name = "attendance_statuses", required = false)List<AttendanceStatus> attendanceStatuses,
      @RequestParam(name = "courses_ids", required = false)List<String> coursesIds,
      @RequestParam(name = "student_key_word", required = false, defaultValue = "")String studentKeyWord,
      @RequestParam(name = "from", required = false)Instant from,
      @RequestParam(name = "to", required = false)Instant to,
      @RequestParam(name = "page")PageFromOne page, @RequestParam(name = "page_size")BoundedPageSize pageSize
      ) {
    List<school.hei.haapi.model.StudentAttendance> toRest = new ArrayList<>(
        attendanceService.getStudentAttendances(studentKeyWord, coursesIds, attendanceStatuses, from, to, page, pageSize)
    );
    return toRest.stream().map(attendanceMapper::toRestAttendance)
        .collect(Collectors.toUnmodifiableList());
  }
}
