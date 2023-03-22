package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Fee;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course,String> {
    List<Course> getByMainTeacherId(String UserId, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE "
            + "(:code IS NULL OR c.code LIKE %:code%) AND "
            + "(:name IS NULL OR c.name LIKE %:name%) AND "
            + "(:credits IS NULL OR c.credits = :credits) AND "
            + "(:teacherFirstName IS NULL OR c.teacher.firstName LIKE %:teacherFirstName%) AND "
            + "(:teacherLastName IS NULL OR c.teacher.lastName LIKE %:teacherLastName%) "
            + "ORDER BY "
            + "CASE WHEN :creditsOrder = 'ASC' THEN c.credits END ASC, "
            + "CASE WHEN :creditsOrder = 'DESC' THEN c.credits END DESC, "
            + "CASE WHEN :codeOrder = 'ASC' THEN c.code END ASC, "
            + "CASE WHEN :codeOrder = 'DESC' THEN c.code END DESC")
    List<Course> findCoursesByCodeAndNameAndCreditsAndTeacherNameOrderByCreditsAndCode(@Param("code") String code,
                                                                                       @Param("name") String name,
                                                                                       @Param("credits") Integer credits,
                                                                                       @Param("teacherFirstName") String teacherFirstName,
                                                                                       @Param("teacherLastName") String teacherLastName,
                                                                                       @Param("creditsOrder") String creditsOrder,
                                                                                       @Param("codeOrder") String codeOrder,
                                                                                       Pageable pageable);

}
