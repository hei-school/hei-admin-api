package school.hei.haapi.repository;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.User;

@Repository
public interface MonitoringStudentRepository extends JpaRepository<User, String> {
  @Query(
      "SELECT m FROM User m JOIN m.monitors s WHERE s.id = :studentId AND m.role = 'MONITOR' AND"
          + " s.role = 'STUDENT'")
  List<User> findAllMonitorsByStudentId(@Param("studentId") String studentId);

  @Query(
      value =
          """
          SELECT u.* FROM "user" u
            LEFT JOIN monitor_following_student mfs ON u.id = mfs.student_id
            WHERE mfs.monitor_id = :monitorId
            AND u.role = 'STUDENT'
          """,
      nativeQuery = true)
  List<User> findAllStudentsByMonitorId(@Param("monitorId") String monitorId, Pageable pageable);

  @Modifying
  @Query(
      value =
          """
          INSERT INTO monitor_following_student (student_id, monitor_id)
            SELECT student.id, :monitorId
            FROM unnest(ARRAY[:studentIds]) AS student(id)
          """,
      nativeQuery = true)
  @Transactional
  void saveMonitorFollowingStudents(String monitorId, Collection<String> studentIds);

  @Query(
      value = "SELECT monitor_id FROM monitor_following_student WHERE student_id = :studentId",
      nativeQuery = true)
  List<String> getAllMonitorsIdsByStudentId(@Param("studentId") String studentId);

  boolean existsByIdAndMonitors_Id(String monitorId, String studentId);
}
