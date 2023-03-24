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
    @Query(
            "select c from Course c where " +
                    "lower(c.code) like concat('%', lower(:code) , '%') and " +
                    "lower(c.name) like concat('%', lower(:name), '%') and " +
                    "lower(c.teacher.firstName) like concat('%', lower(:teacher_first_name), '%') and " +
                    "lower(c.teacher.lastName) like concat('%', lower(:teacher_last_name), '%') and " +
                    "(:credits is null or c.credits = :credits)")
    List<Course> findAllByCriteria(@Param("code") String code,
                                   @Param("name") String name,
                                   @Param("credits") Integer credits,
                                   @Param("teacher_first_name") String teacherFirstName,
                                   @Param("teacher_last_name") String teacherLastName,
                                   Pageable pageable);
}