package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<Course> findByCodeContainingIgnoreCase(String code, Pageable pageable);
    List<Course> findByCredits(int credits, Pageable pageable);
}
