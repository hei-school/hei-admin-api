package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.RetakeExam;

@Component
public interface RetakeExamRepository extends JpaRepository<RetakeExam, String> {
  @Query("select re from RetakeExam re where re.retakeExamSession.id = :retakeExamSessionId ")
  List<RetakeExam> findByRetakeExamSessionId(
      @Param("retakeExamSessionId") String retakeExamSessionId);

  @Query(
      "select re from RetakeExam re where re.retakeExamSession.id = :retakeExamSessionId and"
          + " re.student.id= :studentId")
  List<RetakeExam> findByRetakeExamSessionIdAndStudentId(String sessionId, String studentId);

  boolean existsByStudentIdAndCourseAssignmentIdAndRetakeExamSessionId(
      String studentId, String courseAssignmentId, String sessionId);

  List<RetakeExam> findByStudentId(String studentId);
}
