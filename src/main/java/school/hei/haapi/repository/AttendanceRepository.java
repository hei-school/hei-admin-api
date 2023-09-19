package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.StudentAttendance;

@Repository
public interface AttendanceRepository extends JpaRepository<StudentAttendance, String> {
  @Query(
      "SELECT a FROM StudentAttendance a " +
      "JOIN a.courseSession cs " +
      "WHERE cs.id IN :coursesId " +
      "AND cs.begin BETWEEN :from AND :to"
  )
  List<StudentAttendance> findByCoursesSessionCriteria(
      @Param("coursesId") List<String> coursesId, @Param("from") Instant from, @Param("to") Instant to, Pageable pageable
  );

  @Query(
      "SELECT a FROM StudentAttendance a " +
      "WHERE a.createdAt IS NULL"
  )
  List<StudentAttendance> findStudentsAbsent(Pageable pageable);

  @Query(
      "SELECT a FROM StudentAttendance a " +
      "WHERE a.isLate = :attendanceStatus"
  )
  List<StudentAttendance> findStudentByAttendanceStatus(boolean attendanceStatus, Pageable pageable);
}
