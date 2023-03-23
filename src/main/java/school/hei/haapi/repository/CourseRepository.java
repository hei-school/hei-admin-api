package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.haapi.model.Course;

public interface CourseRepository extends JpaRepository<Course,String> {

    @Query("SELECT c.name, c.credits, c.main_teacher.id FROM Course c WHERE c.code = :code")
    Object[] findNameCreditsAndTeacherIdByCode(@Param("code") String code);

}
