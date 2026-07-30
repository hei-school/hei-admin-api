package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
  List<Transaction> findTransactionsByCredit_Id(String creditId, Pageable pageable);
}
