package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.EventLocation;
import school.hei.haapi.endpoint.rest.model.StudentGlobalAttendance;
import school.hei.haapi.model.StudentAttendance;

@Component
@AllArgsConstructor
public class AttendanceRestMapper {
  private RoomMapper roomMapper;
  private PlaceMapper placeMapper;

  public StudentGlobalAttendance toRest(StudentAttendance s) {
    return new StudentGlobalAttendance()
        .title(s.eventTitle())
        .description(s.eventDescription())
        .location(
            new EventLocation()
                .room(roomMapper.toRest(s.room()))
                .place(placeMapper.toRest(s.place())))
        .eventType(s.eventType())
        .attendanceStatus(s.attendanceStatus())
        .beginDatetime(s.beginDatetime())
        .endDatetime(s.endDatetime());
  }
}
