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
    @Query(value = "SELECT c.id, c.code, c.name, c.credits, c.total_hours, c.main_teacher_id "
            +"FROM course c inner join \"user\" u "
            +"ON c.main_teacher_id=u.id "
            +"WHERE u.first_name ILIKE CONCAT('%',:first_name,'%') "
            +"OR u.last_name ILIKE CONCAT('%',:last_name,'%') ",
            nativeQuery = true)
    List<Course> getByCriteria(@Param("first_name")String first_name,
                      @Param("last_name")String last_name,
                      Pageable pageable);
}
