package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.EventLocation;
import school.hei.haapi.endpoint.rest.model.StudentGlobalAttendance;
import school.hei.haapi.model.dto.StudentAttendanceStatusDto;

@Component
@AllArgsConstructor
public class AttendanceRestMapper {
  private RoomMapper roomMapper;
  private PlaceMapper placeMapper;

  public StudentGlobalAttendance toRest(StudentAttendanceStatusDto dto) {
    return new StudentGlobalAttendance()
        .title(dto.getEventTitle())
        .description(dto.getEventDescription())
        .location(
            new EventLocation()
                .room(roomMapper.toRest(dto.getRoom()))
                .place(placeMapper.toRest(dto.getPlace())))
        .eventType(dto.getEventType())
        .attendanceStatus(dto.getAttendanceStatus())
        .beginDatetime(dto.getBeginDatetime())
        .endDatetime(dto.getEndDatetime());
  }
}
