package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
  Optional<Course> findById(String id);
}
