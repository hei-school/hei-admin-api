package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.StatusCheckResult;
import school.hei.haapi.model.StatusCheck;

@Repository
public interface StatusCheckRepository extends JpaRepository<StatusCheck, String> {
  List<StatusCheck> findAllByConcernedStudentId(String studentId);

  List<StatusCheck> findAllByResult(StatusCheckResult statusCheckResult);
}
