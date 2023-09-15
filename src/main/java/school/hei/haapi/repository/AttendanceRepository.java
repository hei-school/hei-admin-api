package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.StudentAttendance;

@Repository
public interface AttendanceRepository extends JpaRepository<StudentAttendance, String> {
  @Query(
      "SELECT a FROM StudentAttendance a " +
      "WHERE a.courseSession.id  IN ?1"
  )
  List<StudentAttendance> findByCoursesId(List<String> coursesId);
}
