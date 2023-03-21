package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {
    Course findCourseById(String id);

    @Override
    List<Course> findAll();
}
