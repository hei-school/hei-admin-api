package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;

import java.util.List;

@Service
public interface CourseRepository extends JpaRepository<Course, String> {
    @Query(":Query")
    List<Course> getCourseAndFilter(
            @Param("Query") String query
    );
}
