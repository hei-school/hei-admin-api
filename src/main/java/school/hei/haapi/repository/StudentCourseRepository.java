package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.StudentCourse;

import java.util.List;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, String> {
        List<StudentCourse> findAllByStudent_id(String student_id);

        StudentCourse findByCourse_IdAndStudent_Id(String course_id,String student_id);
}
