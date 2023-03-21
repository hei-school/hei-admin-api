package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Courses;

@Repository
public interface CoursesRepository extends JpaRepository<Courses, String> {
}
