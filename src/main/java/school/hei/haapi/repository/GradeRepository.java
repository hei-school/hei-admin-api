package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Grade;

@Repository
public interface GradeRepository extends JpaRepository<Grade, String> {
  @Query("select g from Grade g where  g.exam.id = :exam_id and g.student.id = :student_id")
  Optional<Grade> getGradeByExamIdAndStudentIdAndAwardedCourseIdAndGroupId(
      @Param("exam_id") String examId, @Param("student_id") String studentId);

  Optional<Grade> findByExamIdAndStudentId(String exam_id, String student_id);
}
