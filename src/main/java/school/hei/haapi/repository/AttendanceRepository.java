package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;
import school.hei.haapi.model.CourseSession;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.User;

@Repository
public interface AttendanceRepository extends JpaRepository<StudentAttendance, String> {
  Optional<StudentAttendance> findStudentAttendanceByCourseSessionAndStudent(CourseSession courseSession, User student);

  @Query(
      "SELECT a FROM StudentAttendance a " +
          "LEFT JOIN User u on a.student = u " +
          "LEFT JOIN CourseSession cs on a.courseSession = cs " +
          "WHERE cs.begin BETWEEN :begin AND :end " +
          "AND u = :student " +
          "AND a.attendanceMovementType = 'IN'"
  )
  Optional<StudentAttendance> findStudentAttendanceByFromCourseAndEndCourseAndStudent(
      @Param(value = "begin")Instant begin, @Param(value = "end")Instant end,
      @Param(value = "student")User student
  );

  @Query(
      "SELECT a FROM StudentAttendance a " +
          "LEFT JOIN User u on a.student = u " +
          "WHERE a.createdAt BETWEEN :begin AND :end " +
          "AND u = :student " +
          "AND a.attendanceMovementType = 'IN'"
  )
  Optional<StudentAttendance> findStudentAttendanceFromAndToAndStudent(
      @Param(value = "begin")Instant begin, @Param(value = "end")Instant end,
      @Param(value = "student") User student
  );

  @Query(
      "SELECT a FROM StudentAttendance a " +
          "WHERE a.createdAt IS NULL AND a.attendanceMovementType = 'IN'"
  )
  List<StudentAttendance> findStudentsAbsent(Pageable pageable);

  @Query(
      "SELECT a FROM StudentAttendance a " +
          "WHERE a.isLate = TRUE AND a.attendanceMovementType = 'IN'"
  )
  List<StudentAttendance> findStudentLate(Pageable pageable);

  @Query(
      "SELECT a FROM StudentAttendance a " +
          "WHERE a.isLate = FALSE " +
          "AND a.createdAt IS NOT NULL " +
          "AND a.attendanceMovementType = 'IN'"
  )
  List<StudentAttendance> findStudentPresent(Pageable pageable);

  @Query(
      "SELECT a FROM StudentAttendance a " +
          "WHERE a.createdAt BETWEEN :starting_day AND :ending_day " +
          "AND a.attendanceMovementType = 'IN'"
  )
  List<StudentAttendance> findStudentAttendancesOfTheDay(
      @Param(value = "starting_day") Instant startingDay,
      @Param(value = "ending_day") Instant endingDay
  );

  @Query(
      "SELECT a FROM StudentAttendance a " +
          "WHERE a.attendanceMovementType = 'OUT' " +
          "AND a.createdAt BETWEEN :starting_delay AND :ending_delay"
  )
  List<StudentAttendance> findStudentEscape(
      @Param(value = "starting_delay")Instant startingDelay,
      @Param(value = "ending_delay")Instant endingDelay
  );
}