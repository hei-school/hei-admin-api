package school.hei.haapi.model.psp;

import java.util.List;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PaymentId;

public interface Psp {
  Payment create(PspType pspType, String pspId, String email);

  Payment get(PaymentId paymentId);

  List<Payment> getPayments(List<PaymentId> paymentId);
}
