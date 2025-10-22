package school.hei.haapi.model.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.model.Event.PlaceName;
import school.hei.haapi.model.Event.RoomName;
import school.hei.haapi.model.StudentAttendanceStatus;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class StudentAttendanceStatusDto implements StudentAttendanceStatus {
  private String eventTitle;
  private String eventDescription;
  private EventType eventType;
  private AttendanceStatus attendanceStatus;
  private Instant beginDatetime;
  private Instant endDatetime;
  private RoomName room;
  private PlaceName place;

  public StudentAttendanceStatusDto(StudentAttendanceStatus studentAttendanceStatus) {
    this.eventTitle = studentAttendanceStatus.getEventTitle();
    this.eventDescription = studentAttendanceStatus.getEventDescription();
    this.eventType = studentAttendanceStatus.getEventType();
    this.attendanceStatus = studentAttendanceStatus.getAttendanceStatus();
    this.beginDatetime = studentAttendanceStatus.getBeginDatetime();
    this.endDatetime = studentAttendanceStatus.getEndDatetime();
    this.room = studentAttendanceStatus.getRoom();
    this.place = studentAttendanceStatus.getPlace();
  }
}
