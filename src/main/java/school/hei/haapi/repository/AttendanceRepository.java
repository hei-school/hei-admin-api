package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.StudentAttendance;

@Repository
public interface AttendanceRepository extends JpaRepository<StudentAttendance, String> {

  @Query(
      "SELECT a FROM StudentAttendance a " +
          "WHERE a.createdAt IS NULL"
  )
  List<StudentAttendance> findStudentsAbsent(Pageable pageable);

  @Query(
      "SELECT a FROM StudentAttendance a " +
          "WHERE a.isLate = TRUE"
  )
  List<StudentAttendance> findStudentLate(Pageable pageable);

  @Query(
      "SELECT a FROM StudentAttendance a " +
          "WHERE a.isLate = FALSE " +
          "AND a.createdAt IS NOT NULL"
  )
  List<StudentAttendance> findStudentPresent(Pageable pageable);
}