package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;

import java.util.List;
@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
  List<Course> findCoursesByCreditsIsNullOrCreditsEqualsAndCodeIsContainingIgnoreCaseAndNameContainingIgnoreCaseAndMain_teacherContainingIgnoreCaseAndCodeIsContainingIgnoreCase(
          String name,
          String code,
          Integer credits,
          String teacher_first_name,
          String teacher_last_name,
          Pageable pageable
  );
}
