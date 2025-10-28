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
    WHERE participant.participant.ref = :studentReference
        AND participant.status = :attendanceStatus
        AND participant.event.beginDatetime >= :fromDatetime
        AND participant.event.endDatetime <= :toDatetime
        AND participant.event.title ILIKE COALESCE(:titlePattern, '%%')
""")
  List<StudentAttendance> findStudentAttendances(
      String studentReference,
      AttendanceStatus attendanceStatus,
      Instant fromDatetime,
      Instant toDatetime,
      String titlePattern);
}
