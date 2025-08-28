package school.hei.haapi.endpoint.rest.controller;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;
import static school.hei.haapi.endpoint.rest.model.EventType.COURSE;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.AttendanceRestMapper;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.StudentGlobalAttendance;
import school.hei.haapi.model.StudentAttendanceStatus;
import school.hei.haapi.service.AttendanceService;

class AttendanceControllerTest {
  AttendanceRestMapper attendanceRestMapper = new AttendanceRestMapper();
  AttendanceService attendanceServiceMock = mock();
  AttendanceController subject =
      new AttendanceController(attendanceServiceMock, attendanceRestMapper);

  @Test
  void return_student_global_attendance() {
    var studentId = randomUUID().toString();
    var from = now();
    var to = now().plus(1L, DAYS);
    var attendanceStatus = MISSING;

    when(attendanceServiceMock.getStudentAttendanceByStudentId(
            studentId, attendanceStatus, from, to))
        .thenReturn(List.of(studentAttendanceStatus(attendanceStatus, from, to)));

    var actual = subject.getStudentAttendance(studentId, from, to, attendanceStatus);

    assertEquals(
        List.of(
            new StudentGlobalAttendance()
                .eventTitle("eventTile")
                .eventDescription("eventDescription")
                .eventType(COURSE)
                .attendanceStatus(MISSING)
                .beginDatetime(from)
                .endDatetime(to)),
        actual);
  }

  private StudentAttendanceStatus studentAttendanceStatus(
      AttendanceStatus attendanceStatus, Instant from, Instant to) {
    return new StudentAttendanceStatus(
        "eventTile", "eventDescription", COURSE, attendanceStatus, from, to);
  }
}
