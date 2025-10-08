package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.RetakeExam;

@Repository
public interface RetakeExamRepository extends JpaRepository<RetakeExam, String> {
  Optional<RetakeExam> findByCourse_IdAndStudent_IdAndSession_Id(
      String courseId, String studentId, String sessionId);

  List<RetakeExam> findRetakeExamsBySession_IdAndCourse_Code(
      String retakeExamSessionId, String courseCode, Pageable pageable);

  List<RetakeExam> findRetakeExamsBySession_IdAndStudent_Id(String sessionId, String studentId);

  List<RetakeExam> findRetakeExamsBySession_IdAndCourse_IdAndStudent_Ref(
      String sessionId, String courseId, String studentRef, Pageable pageable);
}
