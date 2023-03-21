package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.model.CourseStudent;

import java.util.List;

@Repository
public interface CourseStudentRepository extends JpaRepository<CourseStudent, String> {
    List<CourseStudent> getAllByStudentIdAndStatus(String student_id, CourseStatus status);
}
