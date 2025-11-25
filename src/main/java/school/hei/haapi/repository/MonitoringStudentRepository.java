package school.hei.haapi.repository;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.MonitorStudentLinkDto;
import school.hei.haapi.model.dto.MonitorStudentLinkDto.Status;

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
            AND mfs.status = 'LINKED'
            AND u.role = 'STUDENT'
          """,
      nativeQuery = true)
  List<User> findAllStudentsByMonitorId(@Param("monitorId") String monitorId, Pageable pageable);

  /**
   * @param studentIds must not be empty
   */
  @Modifying
  @Query(
      value =
          """
          INSERT INTO monitor_following_student (student_id, monitor_id, status)
            SELECT student.id, :monitorId, cast(:status as mfs_status)
            FROM "user" student WHERE student.id in :studentIds
          """,
      nativeQuery = true)
  @Transactional
  void saveMonitorFollowingStudents(String monitorId, List<String> studentIds, String status);

  @Query(
      value = "SELECT monitor_id FROM monitor_following_student WHERE student_id = :studentId",
      nativeQuery = true)
  List<String> getAllMonitorsIdsByStudentId(@Param("studentId") String studentId);

  @Query(
      value =
          """
select mfs.id, mfs.monitor_id, mfs.student_id, mfs.status from monitor_following_student mfs
where mfs.status = 'PENDING'
""",
      nativeQuery = true)
  Slice<MonitorStudentLinkDto> getAllMonitorStudentLinkRequests(Pageable pageable);

  @Query(
      value =
          """
select mfs.id, mfs.monitor_id, mfs.student_id, mfs.status from monitor_following_student mfs
where mfs.id in (:ids)
""",
      nativeQuery = true)
  List<MonitorStudentLinkDto> getMonitorStudentLinkByIds(List<String> ids);

  @Modifying
  @Query(
      value =
          """
          UPDATE monitor_following_student
          SET status = :status
          WHERE id = :monitorFollowingStudentId
          """,
      nativeQuery = true)
  @Transactional
  void updateMonitorFollowingStudentStatus(String monitorFollowingStudentId, Status status);

  boolean existsByIdAndMonitors_Id(String monitorId, String studentId);
}
