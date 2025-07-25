package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Grade;

@Repository
public interface GradeRepository extends JpaRepository<Grade, String> {
  @Query("select g from Grade g where  g.exam.id = :exam_id and g.student.id = :student_id")
  Optional<Grade> getGradeByExamIdAndStudentId(
      @Param("exam_id") String examId, @Param("student_id") String studentId);

  @Query("select g from Grade g where  g.exam.id = :exam_id and g.student.ref = :student_ref")
  Optional<Grade> getGradeByExamIdAndStudentRef(
      @Param("exam_id") String examId, @Param("student_ref") String studentRef);

  Optional<Grade> findByExamIdAndStudentId(String examId, String studentId);

  List<Grade> findAllByStudentId(String studentId);
}
