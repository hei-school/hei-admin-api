package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.StudentAttendanceStatus;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

  @Query("SELECT e FROM Event e WHERE e.beginDatetime BETWEEN :from AND :to")
  List<Event> findEventsBetweenInstant(Instant from, Instant to);

  @Query(
      value =
          """
    SELECT new school.hei.haapi.model.StudentAttendanceStatus(
        eventParticipant.event.title,
        eventParticipant.event.description,
        eventParticipant.event.type,
        eventParticipant.status,
        eventParticipant.event.beginDatetime,
        eventParticipant.event.endDatetime,
        eventParticipant.event.room,
        eventParticipant.event.place
    )
    FROM EventParticipant eventParticipant
    WHERE eventParticipant.participant.ref = :reference
        AND eventParticipant.status = :attendanceStatus
        AND eventParticipant.event.beginDatetime >= :from
        AND eventParticipant.event.endDatetime <= :to
        AND eventParticipant.event.title ILIKE COALESCE(:title, '%%')
""")
  List<StudentAttendanceStatus> getStudentAttendance(
      String reference, AttendanceStatus attendanceStatus, Instant from, Instant to, String title);
}
