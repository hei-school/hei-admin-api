package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    @Query(value = "select c from Course c WHERE " +
            "lower(c.code) like lower(concat('%', cast(:code as text), '%')) " +
            "OR lower(c.name) like lower(concat('%', cast(:name as text), '%')) " +
            "OR lower(c.mainTeacher.firstName) like lower(concat('%', cast(:teacher_first_name as text), '%')) " +
            "OR lower(c.mainTeacher.lastName) like lower(concat('%', cast(:teacher_last_name as text), '%'))" +
            "OR (:credits is null or c.credits = :credits) ")
    List<Course> findCoursesWithParams(
            @Param("name") String name,
            @Param("code") String code,
            @Param("teacher_first_name")String teacherFirstName,
            @Param("teacher_last_name")String teacherLastName,
            @Param("credits") Integer credits,
            Pageable pageable);

    List<Course> findByMainTeacherFirstName(String first_name, Pageable pageable);
    List<Course> findByMainTeacherLastName(String last_name, Pageable pageable);
}
