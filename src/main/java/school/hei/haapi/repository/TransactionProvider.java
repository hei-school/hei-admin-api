package school.hei.haapi.repository;

import java.util.List;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Mpbs.Mpbs;

public interface TransactionProvider {
  TransactionDetails findTransactionByMpbs(Mpbs mpbs);

  List<TransactionDetails> fetchThenSaveTransactionDetails();
}
