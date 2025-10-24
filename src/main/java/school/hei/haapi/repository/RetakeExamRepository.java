package school.hei.haapi.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.model.RetakeExamStatus;

@Repository
public interface RetakeExamRepository extends JpaRepository<RetakeExam, String> {
  List<RetakeExam> findRetakeExamsBySession_IdAndCourse_Code(
      String retakeExamSessionId, String courseCode, Pageable pageable);

  List<RetakeExam> findRetakeExamsBySession_IdAndStudent_Id(String sessionId, String studentId, Pageable pageable);

    @Query("""
    select re from RetakeExam re
    join re.session s
    where re.student.id = :studentId
    and (
        s.id = :currentSessionId
        or (
            re.status not in :excludedStatuses
            and s.dateTo > :currentDate
        )
    )
""")
    List<RetakeExam> findActiveAndCurrentSessionRetakeExams(
            String currentSessionId,
            String studentId,
            Collection<RetakeExamStatus> excludedStatuses,
            Instant currentDate
            );

}
