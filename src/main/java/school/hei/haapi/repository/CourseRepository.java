package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    Course findCourseById(String courseId);

    Course findCourseByCode(String code);

}

