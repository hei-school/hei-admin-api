package school.hei.haapi.model;

import java.time.Instant;
import java.util.Objects;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.model.Event.PlaceName;
import school.hei.haapi.model.Event.RoomName;

public record StudentAttendance(
    String eventId,
    String eventTitle,
    String eventDescription,
    EventType eventType,
    AttendanceStatus attendanceStatus,
    Instant beginDatetime,
    Instant endDatetime,
    RoomName room,
    PlaceName place) {

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StudentAttendance other)) return false;
    return Objects.equals(eventTitle, other.eventTitle)
        && Objects.equals(eventDescription, other.eventDescription)
        && eventType == other.eventType
        && attendanceStatus == other.attendanceStatus
        && Objects.equals(beginDatetime, other.beginDatetime)
        && Objects.equals(endDatetime, other.endDatetime)
        && Objects.equals(room, other.room)
        && Objects.equals(place, other.place);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        eventTitle,
        eventDescription,
        eventType,
        attendanceStatus,
        beginDatetime,
        endDatetime,
        room,
        place);
  }
}
