package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.repository.MobilePaymentRepository;
import school.hei.haapi.service.mobileMoney.MobileMoneyApi;

@Service
@AllArgsConstructor
public class MobilePaymentService implements MobilePaymentRepository {
  private final MobileMoneyApi mobileMoneyApi;

  @Override
  public TransactionDetails findTransactionByMpbs(Mpbs mpbs) {
    String transactionRef = mpbs.getPspId();
    return mobileMoneyApi.getByTransactionRef(mpbs.getMobileMoneyType(), transactionRef);
  }
}
