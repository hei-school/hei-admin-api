package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.StudentCourse;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, String> {
  List<StudentCourse> findAllByStudent_id(String student_id);

  StudentCourse findByCourse_IdAndStudent_Id(String course_id, String student_id);
}
