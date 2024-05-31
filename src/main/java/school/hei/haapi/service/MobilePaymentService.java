package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.repository.MobilePaymentRepository;
import school.hei.haapi.service.mobileMoney.MobileMoneyApi;
import school.hei.haapi.service.mobileMoney.MobileMoneyApiFacade;

@Service
@AllArgsConstructor
public class MobilePaymentService implements MobilePaymentRepository {
  private final MobileMoneyApi mobileMoneyApi;

  @Override
  public TransactionDetails findTransactionByMpbs(Mpbs mpbs) throws ApiException {
    String transactionRef = mpbs.getPspId();
    return mobileMoneyApi.getByTransactionRef(mpbs.getMobileMoneyType(), transactionRef);
  }
}
