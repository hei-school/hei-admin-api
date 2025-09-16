package school.hei.haapi.model;

import java.time.Instant;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventType;

public record StudentAttendanceStatus(
    String eventTitle,
    String eventDescription,
    EventType eventType,
    AttendanceStatus attendanceStatus,
    Instant beginDatetime,
    Instant endDatetime,
    Event.RoomName room,
    Event.PlaceName place) {}
