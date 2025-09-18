package school.hei.haapi.service.mobileMoney;

import java.util.List;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Mpbs.Mpbs;

public interface MobileTransactionProvider {
  TransactionDetails findTransactionByMpbs(Mpbs mpbs);

  List<TransactionDetails> fetchThenSaveTransactionDetails();
}
