package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.CourseAssignment;

@Repository
public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, String> {
  @Query("SELECT ca FROM CourseAssignment ca JOIN ca.groups g WHERE ca.id = :id AND g.id = :groupId")
  CourseAssignment getByIdAndGroupId(String id, String groupId);

  @Query("SELECT ca FROM CourseAssignment ca JOIN ca.groups g WHERE g.id = :groupId")
  List<CourseAssignment> findAllByGroupId(String groupId, Pageable pageable);

  List<CourseAssignment> findAllByMainTeacherId(String teacherId, Pageable pageable);

  List<CourseAssignment> findAllByCourseId(String courseId, Pageable pageable);
}
