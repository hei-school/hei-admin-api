package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    List<Course> getCoursesByMainTeacher_FirstNameIgnoreCase(@NotBlank(message = "First name is mandatory") String mainTeacher_firstName);

    List<Course> getCoursesByMainTeacher_LastNameIgnoreCase(String mainTeacher_lastName);
    List<Course> getCoursesByCodeIgnoreCase(String code);

    List<Course> getCourseByNameIgnoreCase(String name);

    List<Course> getCoursesByCredits(Integer credits);
}
