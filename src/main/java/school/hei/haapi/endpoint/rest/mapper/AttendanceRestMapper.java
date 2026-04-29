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

  public StudentGlobalAttendance toRest(StudentAttendance domain) {
    return new StudentGlobalAttendance()
        .id(domain.eventId())
        .title(domain.eventTitle())
        .description(domain.eventDescription())
        .location(
            new EventLocation()
                .room(roomMapper.toRest(domain.room()))
                .place(placeMapper.toRest(domain.place())))
        .eventType(domain.eventType())
        .attendanceStatus(domain.attendanceStatus())
        .beginDatetime(domain.beginDatetime())
        .endDatetime(domain.endDatetime());
  }
}
