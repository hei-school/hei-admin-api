package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.CourseAssignment;

@Repository
public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, String> {
  CourseAssignment getByIdAndGroupId(String Id, String groupId);

  List<CourseAssignment> findAllByGroupId(String groupId, Pageable pageable);

  List<CourseAssignment> findAllByMainTeacherId(String teacherId, Pageable pageable);
}
