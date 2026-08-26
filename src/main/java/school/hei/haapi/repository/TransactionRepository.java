package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.CreditMovement;
import school.hei.haapi.model.CreditTransaction;

public interface TransactionRepository extends JpaRepository<CreditTransaction, String> {
  List<CreditTransaction> findTransactionsByCredit_Id(String creditId, Pageable pageable);

  boolean existsByFee_IdAndCreditMovement(String feeId, CreditMovement creditMovement);
}
