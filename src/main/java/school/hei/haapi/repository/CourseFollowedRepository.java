package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.model.CourseFollowed;

public interface CourseFollowedRepository extends JpaRepository<CourseFollowed, String> {
  List<CourseFollowed> findAllByStudentIdAndStatus(String studentId, CourseStatus status);
}
