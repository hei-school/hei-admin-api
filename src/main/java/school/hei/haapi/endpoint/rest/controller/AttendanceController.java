package school.hei.haapi.endpoint.rest.controller;


import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.CreateAttendanceMovement;
import school.hei.haapi.endpoint.rest.model.StudentAttendance;
import school.hei.haapi.endpoint.rest.model.StudentAttendanceMovement;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.NotImplementedException;

@RestController
public class AttendanceController {

  @PostMapping("/attendance/movement")
  public StudentAttendanceMovement createAttendanceMovement (@RequestBody CreateAttendanceMovement movement) {
    throw new NotImplementedException("this endpoint is not yet implemented");
  }

  @GetMapping("/attendance")
  public List<StudentAttendance> getStudentsAttendance(

      @RequestParam(name = "page")PageFromOne page, @RequestParam(name = "page_size")BoundedPageSize pageSize,
      @RequestParam(name = "courses_ids", required = false)List<String> coursesIds,
      @RequestParam(name = "teachers_ids", required = false)List<String> teacherIds,
      @RequestParam(name = "student_key_word", required = false, defaultValue = "")String studentKeyWord,
      @RequestParam(name = "from", required = false)Instant from,
      @RequestParam(name = "to", required = false)Instant to,
      @RequestParam(name = "attendance_statuses", required = false)List<AttendanceStatus> attendanceStatuses
  ) {
    throw new NotImplementedException("this endpoint is not yet implemented");
  }
}