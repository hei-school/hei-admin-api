package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.User;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, String> {
  StudentCourse findByUserIdAndCourseId(User userId, Course courseId);
  StudentCourse findByUserId(User userId);
}
