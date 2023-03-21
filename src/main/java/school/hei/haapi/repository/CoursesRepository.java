package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.Courses;

public interface CoursesRepository extends JpaRepository<Courses, String> {

}
