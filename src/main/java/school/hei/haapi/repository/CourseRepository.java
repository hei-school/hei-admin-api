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
   /**
    @Query("SELECT c FROM Course c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Course> getCourseByCourseNameContainingIgnoreCase(
            @Param("name") String name,
            Pageable pageable);

    @Query("SELECT c FROM Course c JOIN User u ON c.mainTeacherId = u.id WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))")
    List<Course> getCourseByTeacherFirstNameContainingIgnoreCase(
            @Param("firstName") String firstName,
            Pageable pageable
    );

    @Query("SELECT c FROM Course c JOIN User u ON c.mainTeacherId = u.id WHERE LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Course> getCourseByTeacherLastNameContainingIgnoreCase(
            @Param("lastName") String lastName,
            Pageable pageable
    );

    @Query("SELECT c FROM Course c JOIN User u ON c.mainTeacherId = u.id WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) AND LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Course> getCourseByTeacherFirstNameContainingIgnoreCaseAndTeacherLastNameContainingIgnoreCase(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            Pageable pageable
    );
            **/
}
