package school.hei.haapi.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventType;

import static school.hei.haapi.model.Event.*;

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
