package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> getByStudentId(String studentId, Pageable pageable);

    List<Course> getCourseByStudentId(String studentId, Pageable pageable, Course.StatusEnum status);
}