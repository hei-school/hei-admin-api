package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;

@Service
public interface CourseRepository extends JpaRepository<Course, String> {

}
