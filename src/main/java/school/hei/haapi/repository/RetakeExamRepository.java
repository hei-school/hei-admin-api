package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.RetakeExam;

@Component
public interface RetakeExamRepository extends JpaRepository<RetakeExam, String> {
  Optional<RetakeExam> findByCourse_IdAndStudent_IdAndSession_Id(
      String courseId, String studentId, String sessionId);

  List<RetakeExam> findRetakeExamsBySession_Id(@Param("sessionId") String retakeExamSessionId);

  List<RetakeExam> findRetakeExamsBySession_IdAndStudent_Id(String sessionId, String studentId);
}
