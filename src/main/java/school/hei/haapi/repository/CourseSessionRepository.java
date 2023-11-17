package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.CourseSession;
import school.hei.haapi.model.StudentAttendance;

@Repository
public interface CourseSessionRepository extends JpaRepository<CourseSession, String> {
  @Query(
      "SELECT cs FROM CourseSession cs " +
          "WHERE cs.begin BETWEEN :starting_day " +
          "AND :ending_day " +
          "ORDER BY cs.begin ASC"
  )
  List<CourseSession> findCoursesSessionsOfTheDay(
      @Param(value = "starting_day") Instant startingDay,
      @Param(value = "ending_day") Instant endingDay
  );
}