package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByNameLikeIgnoreCase(String name, Pageable pageable);
    List<Course> findByCodeLikeIgnoreCase(String code, Pageable pageable);
    List<Course> findByCredits(Integer credits, Pageable pageable);
    List<Course> findByMainTeacherFirstName(String first_name, Pageable pageable);
    List<Course> findByMainTeacherLastName(String last_name, Pageable pageable);
}
