package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.EventLocation;
import school.hei.haapi.endpoint.rest.model.StudentGlobalAttendance;
import school.hei.haapi.model.StudentAttendanceStatus;
import school.hei.haapi.model.StudentAttendanceStatusRepresentation;

@Component
@AllArgsConstructor
public class AttendanceRestMapper {
  private RoomMapper roomMapper;
  private PlaceMapper placeMapper;

  public StudentGlobalAttendance toRest(StudentAttendanceStatusRepresentation s) {
    return new StudentGlobalAttendance()
        .title(s.getEventTitle())
        .description(s.getEventDescription())
        .location(
            new EventLocation()
                .room(roomMapper.toRest(s.getRoom()))
                .place(placeMapper.toRest(s.getPlace())))
        .eventType(s.getEventType())
        .attendanceStatus(s.getAttendanceStatus())
        .beginDatetime(s.getBeginDatetime())
        .endDatetime(s.getEndDatetime());
  }
}
