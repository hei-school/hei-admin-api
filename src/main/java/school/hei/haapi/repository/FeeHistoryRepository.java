package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.FeesHistory;

import java.util.List;

@Repository
public interface FeeHistoryRepository extends JpaRepository<FeesHistory , String>{
  FeesHistory getByStudentId(String studentId);
  List<FeesHistory> getFeesHistoryByPaid(Boolean paid);
}
