package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.Credit;

public interface CreditRepository extends JpaRepository<Credit, String> {
  Credit findCreditByStudent_Id(String studentId);
}
