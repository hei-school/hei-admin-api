package school.hei.haapi.model;

import java.time.Instant;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.model.Event.PlaceName;
import school.hei.haapi.model.Event.RoomName;

public record StudentAttendanceStatus(
    String eventTitle,
    String eventDescription,
    EventType eventType,
    AttendanceStatus attendanceStatus,
    Instant beginDatetime,
    Instant endDatetime,
    RoomName room,
    PlaceName place) {}
