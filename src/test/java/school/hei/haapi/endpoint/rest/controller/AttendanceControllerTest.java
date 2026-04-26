package school.hei.haapi.endpoint.rest.controller;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.AttendanceStatus.MISSING;
import static school.hei.haapi.endpoint.rest.model.EventType.COURSE;
import static school.hei.haapi.model.Event.PlaceName.ANDRAHARO;
import static school.hei.haapi.model.Event.PlaceName.IVANDRY;
import static school.hei.haapi.model.Event.RoomName.ALGEBRE;
import static school.hei.haapi.model.Event.RoomName.PI;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.AttendanceRestMapper;
import school.hei.haapi.endpoint.rest.mapper.PlaceMapper;
import school.hei.haapi.endpoint.rest.mapper.RoomMapper;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventLocation;
import school.hei.haapi.endpoint.rest.model.PlaceEnum;
import school.hei.haapi.endpoint.rest.model.RoomEnum;
import school.hei.haapi.endpoint.rest.model.StudentGlobalAttendance;
import school.hei.haapi.model.Event.PlaceName;
import school.hei.haapi.model.Event.RoomName;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.service.AttendanceService;

class AttendanceControllerTest {
  AttendanceRestMapper attendanceRestMapper =
      new AttendanceRestMapper(new RoomMapper(), new PlaceMapper());
  AttendanceService attendanceServiceMock = mock();
  AttendanceController subject =
      new AttendanceController(attendanceServiceMock, attendanceRestMapper);

  @Test
  void return_student_global_attendance() {
    var studentId = randomUUID().toString();
    var from = now();
    var to = now().plus(1L, DAYS);
    var attendanceStatus = MISSING;

    when(attendanceServiceMock.findStudentAttendanceByStudentId(
            studentId, attendanceStatus, from, to, List.of()))
        .thenReturn(List.of(studentAttendanceStatus(attendanceStatus, from, to, PI, IVANDRY)));

    var actual = subject.getStudentAttendance(studentId, from, to, attendanceStatus, List.of());

    assertThatList(actual)
        .containsOnly(
            new StudentGlobalAttendance()
                .title("eventTile")
                .description("eventDescription")
                .location(new EventLocation().room(RoomEnum.PI).place(PlaceEnum.IVANDRY))
                .eventType(COURSE)
                .attendanceStatus(MISSING)
                .beginDatetime(from)
                .endDatetime(to));
  }

  @Test
  void return_student_global_attendance_filter_by_title() {
    var studentId = randomUUID().toString();
    var from = now();
    var to = now().plus(1L, DAYS);
    var attendanceStatus = MISSING;

    var titlesFilter = List.of("event");
    when(attendanceServiceMock.findStudentAttendanceByStudentId(
            studentId, attendanceStatus, from, to, titlesFilter))
        .thenReturn(
            List.of(studentAttendanceStatus(attendanceStatus, from, to, ALGEBRE, ANDRAHARO)));

    var actual = subject.getStudentAttendance(studentId, from, to, attendanceStatus, titlesFilter);

    assertThatList(actual)
        .containsOnly(
            new StudentGlobalAttendance()
                .title("eventTile")
                .description("eventDescription")
                .location(new EventLocation().room(RoomEnum.ALGEBRE).place(PlaceEnum.ANDRAHARO))
                .eventType(COURSE)
                .attendanceStatus(MISSING)
                .beginDatetime(from)
                .endDatetime(to));
  }

  private static StudentAttendance studentAttendanceStatus(
      AttendanceStatus attendanceStatus, Instant from, Instant to, RoomName room, PlaceName place) {
    return new StudentAttendance(
        "eventId",
        "eventTile",
        "eventDescription",
        COURSE,
        attendanceStatus,
        from,
        to,
        room,
        place);
  }
}
