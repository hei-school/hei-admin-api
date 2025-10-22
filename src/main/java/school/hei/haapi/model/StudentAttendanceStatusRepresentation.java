package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.endpoint.rest.model.EventType;

import java.time.Instant;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class StudentAttendanceStatusRepresentation {
    private String eventTitle;
    private String eventDescription;
    private EventType eventType;
    private AttendanceStatus attendanceStatus;
    private Instant beginDatetime;
    private Instant endDatetime;
    private Event.RoomName room;
    private Event.PlaceName place;

    public StudentAttendanceStatusRepresentation(StudentAttendanceStatus studentAttendanceStatus) {
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
