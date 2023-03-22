package school.hei.haapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    Page<Course> findAll(Pageable pageable);
}
