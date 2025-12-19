package school.hei.haapi.model.psp;

import java.util.List;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PaymentInfo;

public interface Psp {
  Payment create(PspType pspType, String pspId, String email);

  Payment get(PspType pspType, String pspId, String email);

  List<Payment> getPayments(List<PaymentInfo> paymentInfos);
}
