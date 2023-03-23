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
    )
    List<Course> findAllContainsIgnoreCaseByCodeAndNameAndCredits(
            @Param("code") String code,
            @Param("name") String name,
            @Param("credits") Integer credits,
            Pageable pageable
    );

    default List<Course> findAllContainsIgnoreCaseByCodeAndNameAndCreditsAndTeacher(
            String code, String name, Integer credits, String teacherFirstName,
            String teacherLastName, Pageable pageable
    ) {
        return (teacherFirstName != null && teacherLastName == null)
                ? this.findAllContainsIgnoreCaseByCodeAndNameAndCreditsAndTeacher_firstName(
                code, name, credits, teacherFirstName, pageable)
                : (teacherFirstName == null && teacherLastName != null)
                ? this.findAllContainsIgnoreCaseByCodeAndNameAndCreditsAndTeacher_lastName(
                code, name, credits, teacherLastName, pageable)
                : this.findAllContainsIgnoreCaseByCodeAndNameAndCreditsAndTeacher_firstNameAndTeacher_lastName(
                code, name, credits, teacherFirstName, teacherLastName, pageable);
    }

    @Query("SELECT c FROM Course c WHERE LOWER(c.code) LIKE LOWER(CONCAT('%', :code, '%')) "
            + "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) "
            + "AND (:credits IS NULL OR c.credits = :credits) "
            + "AND (LOWER(c.mainTeacher.firstName) LIKE LOWER(CONCAT('%', :teacher_first_name, '%')) "
            + "OR LOWER(c.mainTeacher.lastName) LIKE LOWER(CONCAT('%', :teacher_last_name, '%')))"
    )
    List<Course> findAllContainsIgnoreCaseByCodeAndNameAndCreditsAndTeacher_firstNameAndTeacher_lastName(
            @Param("code") String code,
            @Param("name") String name,
            @Param("credits") Integer credits,
            @Param("teacher_first_name") String teacherFirstName,
            @Param("teacher_last_name") String teacherLastName,
            Pageable pageable
    );

    @Query("SELECT c FROM Course c WHERE LOWER(c.code) LIKE LOWER(CONCAT('%', :code, '%')) "
            + "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) "
            + "AND (:credits IS NULL OR c.credits = :credits) "
            + "AND LOWER(c.mainTeacher.firstName) LIKE LOWER(CONCAT('%', :teacher_first_name, '%'))"
    )
    List<Course> findAllContainsIgnoreCaseByCodeAndNameAndCreditsAndTeacher_firstName(
            @Param("code") String code,
            @Param("name") String name,
            @Param("credits") Integer credits,
            @Param("teacher_first_name") String teacherFirstName,
            Pageable pageable
    );

    @Query("SELECT c FROM Course c WHERE LOWER(c.code) LIKE LOWER(CONCAT('%', :code, '%')) "
            + "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) "
            + "AND (:credits IS NULL OR c.credits = :credits) "
            + "AND LOWER(c.mainTeacher.lastName) LIKE LOWER(CONCAT('%', :teacher_last_name, '%'))"
    )
    List<Course> findAllContainsIgnoreCaseByCodeAndNameAndCreditsAndTeacher_lastName(
            @Param("code") String code,
            @Param("name") String name,
            @Param("credits") Integer credits,
            @Param("teacher_last_name") String teacherLastName,
            Pageable pageable
    );
}
