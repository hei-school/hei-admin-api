package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.User;

import java.util.List;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, String> {
    List<StudentCourse> getStudentCourseByStudentIdAndStatus(String studentId, StudentCourse.CourseStatus status);
    StudentCourse getStudentCourseByStudentIdAndCourseId(String studentId, String courseId);
}
