package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.EventLocation;
import school.hei.haapi.endpoint.rest.model.StudentGlobalAttendance;
import school.hei.haapi.model.StudentAttendanceStatus;

@Component
@AllArgsConstructor
public class AttendanceRestMapper {
  private RoomMapper roomMapper;
  private PlaceMapper placeMapper;

  public StudentGlobalAttendance toRest(StudentAttendanceStatus studentAttendanceStatus) {
    return new StudentGlobalAttendance()
        .title(studentAttendanceStatus.eventTitle())
        .description(studentAttendanceStatus.eventDescription())
        .location(
            new EventLocation()
                .room(roomMapper.toRest(studentAttendanceStatus.room()))
                .place(placeMapper.toRest(studentAttendanceStatus.place())))
        .eventType(studentAttendanceStatus.eventType())
        .attendanceStatus(studentAttendanceStatus.attendanceStatus())
        .beginDatetime(studentAttendanceStatus.beginDatetime())
        .endDatetime(studentAttendanceStatus.endDatetime());
  }
}
