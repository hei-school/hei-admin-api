package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Fee;

@Repository
public interface CourseRespository extends JpaRepository<Course , String> {
    Course getByStudentIdAndId(String studentId, String courseId);
}
