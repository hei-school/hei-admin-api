package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    List<Course> getCoursesByMainTeacherFirstName(String mainTeacher_firstName);

    List<Course> getCoursesByMainTeacherLastName(String mainTeacher_lastName);
    List<Course> getCoursesByCodeContainingIgnoreCase(String code);

    List<Course> getCoursesByNameContainingIgnoreCase(String name);

    List<Course> getCoursesByCredits(Integer credits);
}
