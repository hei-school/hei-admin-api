package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.StudentCourse;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, String> {
  StudentCourse findByStudentIdAndCourseId(String userId, String courseId);

  List<StudentCourse> findByStudentId(String userId);

  List<StudentCourse> findByCourseId(Course courseId);
}
