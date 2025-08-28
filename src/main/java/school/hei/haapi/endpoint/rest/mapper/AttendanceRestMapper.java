package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentGlobalAttendance;
import school.hei.haapi.model.StudentAttendanceStatus;

@Component
public class AttendanceRestMapper {
  public StudentGlobalAttendance toRest(StudentAttendanceStatus s) {
    return new StudentGlobalAttendance()
        .eventTitle(s.eventTitle())
        .eventDescription(s.eventDescription())
        .eventType(s.eventType())
        .attendanceStatus(s.attendanceStatus())
        .beginDatetime(s.beginDatetime())
        .endDatetime(s.endDatetime());
  }
}
