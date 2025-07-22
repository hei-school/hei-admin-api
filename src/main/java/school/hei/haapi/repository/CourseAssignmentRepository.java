package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.User;

@Repository
public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, String> {
  @Query(
      "SELECT ca FROM CourseAssignment ca JOIN ca.groups g WHERE ca.id = :id AND g.id = :groupId")
  CourseAssignment getByIdAndGroupId(@Param("id") String id, @Param("groupId") String groupId);

  @Query("SELECT ca FROM CourseAssignment ca JOIN ca.groups g WHERE g.id = :groupId")
  List<CourseAssignment> findAllByGroupId(@Param("groupId") String groupId, Pageable pageable);

  List<CourseAssignment> findAllByMainTeacher(User teacher, Pageable pageable);

  List<CourseAssignment> findAllByCourseId(String courseId, Pageable pageable);
}
