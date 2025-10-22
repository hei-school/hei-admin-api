package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.StudentAttendanceStatus;
import school.hei.haapi.model.StudentAttendanceStatusRepresentation;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

  @Query("SELECT e FROM Event e WHERE e.beginDatetime BETWEEN :from AND :to")
  List<Event> findEventsBetweenInstant(Instant from, Instant to);

  @Query(
      value =
          "select event_title eventTitle, event_description eventDescription, event_type eventType,"
              + " attendance_status attendanceStatus, begin_datetime beginDatetime, end_datetime endDatetime, room, place from"
              + " get_event_student_attendance(:reference, cast(:attendanceStatus as"
              + " attendance_status), :from, :to, :title)",
      nativeQuery = true)
  List<StudentAttendanceStatus> getStudentAttendance(
      String reference, String attendanceStatus, Instant from, Instant to, String title);
}
