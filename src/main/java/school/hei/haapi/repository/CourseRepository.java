package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.haapi.model.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {
    @Query("SELECT c FROM Course c WHERE LOWER(c.code) LIKE LOWER(CONCAT('%', :code, '%')) "
            + "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) "
            + "AND (:credits IS NULL OR c.credits = :credits) "
            + "AND (LOWER(c.mainTeacher.firstName) LIKE LOWER(CONCAT('%', :teacher_first_name, '%')) "
            + "OR LOWER(c.mainTeacher.lastName) LIKE LOWER(CONCAT('%', :teacher_last_name, '%')))"
    )
    List<Course> findCoursesByCriteria(
            @Param("code") String code,
            @Param("name") String name,
            @Param("credits") Integer credits,
            @Param("teacher_first_name") String teacherFirstName,
            @Param("teacher_last_name") String teacherLastName,
            Pageable pageable
    );
}
