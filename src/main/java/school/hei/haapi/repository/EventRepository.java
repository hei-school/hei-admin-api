package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.StudentAttendance;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

  @Query("SELECT e FROM Event e WHERE e.beginDatetime BETWEEN :from AND :to")
  List<Event> findEventsBetweenInstant(Instant from, Instant to);

  /**
   * @param reference the reference identifier of the student
   * @param attendanceStatus the attendance status to match
   * @param from the start of the datetime range for events
   * @param to the end of the datetime range for events
   * @param titlePattern optional title pattern (supports SQL ILIKE syntax, e.g., "%title%")
   */
  @Query(
      value =
          """
    SELECT new school.hei.haapi.model.StudentAttendance(
        participant.event.title,
        participant.event.description,
        participant.event.type,
        participant.status,
        participant.event.beginDatetime,
        participant.event.endDatetime,
        participant.event.room,
        participant.event.place
    )
    FROM EventParticipant participant
    WHERE participant.participant.ref = :reference
        AND participant.status = :attendanceStatus
        AND participant.event.beginDatetime >= :from
        AND participant.event.endDatetime <= :to
        AND participant.event.title ILIKE COALESCE(:titlePattern, '%%')
""")
  List<StudentAttendance> getStudentAttendance(
      String reference,
      AttendanceStatus attendanceStatus,
      Instant from,
      Instant to,
      String titlePattern);
}
