package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.haapi.model.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course,String> {

    @Query("SELECT c FROM Course c WHERE LOWER(c.code) LIKE LOWER(concat('%', :code, '%'))")
    List<Course> findByCodeContainingIgnoreCase(@Param("code") String code);

    @Query("SELECT c FROM Course c WHERE LOWER(c.name) LIKE LOWER(concat('%', :name,'%'))")
    List<Course> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT c FROM Course c WHERE c.credits = :credits")
    List<Course> findAllByCredits(@Param("credits") int credits);

}
