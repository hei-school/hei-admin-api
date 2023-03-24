package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.StudentCourse;

import java.util.List;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, String> {

    List<StudentCourse> getStudentCourseByStudentIdAndStatus(String studentId,
                                                             StudentCourse.CourseStatus status);
}