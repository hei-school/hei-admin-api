package school.hei.haapi.service.mobileMoney;

import java.util.List;
import java.util.Optional;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Mpbs.Mpbs;

public interface MobileTransactionProvider {
  Optional<TransactionDetails> findTransactionByMpbs(Mpbs mpbs);

  List<TransactionDetails> fetchThenSaveTransactionDetails();
}
