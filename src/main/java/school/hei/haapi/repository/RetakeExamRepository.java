package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.RetakeExam;

@Repository
public interface RetakeExamRepository extends JpaRepository<RetakeExam, String> {
  List<RetakeExam> findRetakeExamsBySession_IdAndCourse_Code(
      String retakeExamSessionId, String courseCode, Pageable pageable);

  List<RetakeExam> findRetakeExamsBySession_IdAndStudent_Id(
      String sessionId, String studentId, Pageable pageable);

  @Query(
      """
        select re from RetakeExam re
        where re.student.id = :studentId
        and (
            re.session.id = :currentSessionId
            or (
                re.status not in ('INVALIDATE','CANCELED')
                and re.session.dateTo > :currentDate
            )
        )
      """)
  List<RetakeExam> findExistingRetakeExamsForCurrentAndFutureSessionsByStudentId(
      String currentSessionId, String studentId, Instant currentDate);
}
