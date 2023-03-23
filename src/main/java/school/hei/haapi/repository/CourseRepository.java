package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    List<Course> getCoursesByMainTeacherFirstName(String mainTeacherFirstName,Pageable pageable);

    List<Course> getCoursesByMainTeacherLastName(String mainTeacherLastName,Pageable pageable);
    List<Course> getCoursesByCodeContainingIgnoreCase(String code,Pageable pageable);

    List<Course> getCoursesByNameContainingIgnoreCase(String name,Pageable pageable);

    List<Course> getCoursesByCredits(Integer credits,Pageable pageable);
}
