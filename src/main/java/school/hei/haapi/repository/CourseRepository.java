package school.hei.haapi.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Course;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface CourseRepository extends JpaRepository<Course, String> {

    List<Course> getCourseByNameContainingIgnoreCase(String name,Pageable pageable);

    @Query("SELECT c FROM Course c WHERE LOWER(c.main_teacher.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))")
    List<Course> getByMainTeacherFirstNameContainingIgnoreCase(
            @Param("firstName") String firstName,
            Pageable pageable);

    @Query("SELECT c FROM Course c WHERE LOWER(c.main_teacher.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Course> getByMainTeacherLastNameContainingIgnoreCase(
            @Param("lastName") String lastName,
            Pageable pageable
            );


    @Query("SELECT c FROM Course c WHERE lower(c.main_teacher.firstName) like %:teacherFirstName% and lower(c.main_teacher.lastName) like %:teacherLastName%")
    List<Course> getCourseByMainTeacherFirstNameAndLastName(
            @Param("teacherFirstName") String teacherFirstName,
            @Param("teacherLastName") String teacherLastName,
            Pageable pageable
            );

}
