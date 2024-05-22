package school.hei.haapi.service.mobileMoney;

import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.http.model.TransactionDetails;

public interface MobileMoneyApi {
  TransactionDetails getByTransactionRef(MobileMoneyType type, String ref);
}
