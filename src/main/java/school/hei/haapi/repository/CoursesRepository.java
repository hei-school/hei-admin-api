package school.hei.haapi.repository;

import java.util.List;

import org.hibernate.mapping.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Courses;


@Repository
public interface CoursesRepository extends JpaRepository<Courses, String> {

    List<Courses> findByCodeContainingIgnoreCase(String code, Pageable pageable);
}
