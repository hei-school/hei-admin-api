package school.hei.haapi.repository;


import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Remedial;

@Repository
public interface RemedialRepository extends JpaRepository<Remedial, String> {
    @Query("select r from Remedial r where r.courseAssignment.course.id = :course_id ")
    List<Remedial> findExamsByCourseId(@Param("course_id") String courseId);
}

