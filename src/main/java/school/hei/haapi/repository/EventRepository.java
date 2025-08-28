package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

  @Query("SELECT e FROM Event e WHERE e.beginDatetime BETWEEN :from AND :to")
  List<Event> findEventsBetweenInstant(Instant from, Instant to);

  @Query(
      value =
          "select event_title, event_description, event_type,"
              + " attendance_status, begin_datetime, end_datetime from"
              + " get_event_student_attendance(:reference, cast(:attendanceStatus as"
              + " attendance_status), :from, :to)",
      nativeQuery = true)
  List<Object[]> getStudentAttendance(
      String reference, String attendanceStatus, Instant from, Instant to);
}
