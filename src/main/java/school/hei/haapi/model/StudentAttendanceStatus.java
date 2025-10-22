package school.hei.haapi.model;

import static school.hei.haapi.model.Event.PlaceName;
import static school.hei.haapi.model.Event.RoomName;

import java.time.Instant;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventType;

public interface StudentAttendanceStatus {
  String getEventTitle();

  String getEventDescription();

  EventType getEventType();

  AttendanceStatus getAttendanceStatus();

  Instant getBeginDatetime();

  Instant getEndDatetime();

  RoomName getRoom();

  PlaceName getPlace();
}
