package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByCodeContainingIgnoreCase(String code, Pageable pageable);
    List<Course> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<Course> findByCredits(Integer credits, Pageable pageable);
    List<Course> findByTeacherFirstNameContainingIgnoreCaseAndTeacherLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
    List<Course> findByTeacherFirstNameContainingIgnoreCase(String firstName, Pageable pageable);
    List<Course> findByTeacherLastNameContainingIgnoreCase(String lastName, Pageable pageable);
}
