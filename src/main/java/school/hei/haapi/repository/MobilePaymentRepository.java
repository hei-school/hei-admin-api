package school.hei.haapi.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Mpbs.Mpbs;

@Repository
public interface MobilePaymentRepository {
  TransactionDetails findTransactionByMpbs(Mpbs mpbs);

  List<TransactionDetails> fetchThenSaveTransactionDetails();
}
