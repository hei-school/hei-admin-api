package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.RetakeExamFee;

@Repository
public interface RetakeExamFeeRepository extends JpaRepository<RetakeExamFee, String> {
  List<RetakeExamFee> findByRetakeExam_IdIn(List<String> retakeExamIds);
}
