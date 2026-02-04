package school.hei.haapi.model.psp.vola.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataRetrievalFailureException;
import school.hei.haapi.model.psp.Psp;
import school.hei.haapi.model.psp.PspType;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PaymentId;

@Slf4j
@AllArgsConstructor
public class VolaPsp implements Psp {
  private final VolaClient volaClient;

  @Override
  public Payment create(PspType pspType, String pspId, String email) {
    try {
      log.info("Creating MPBS via Vola for student: {}", email);
      return volaClient.create(pspType, pspId, email);
    } catch (Exception e) {
      log.error("Error creating MPBS via Vola", e);
      throw new DataRetrievalFailureException("Failed to create MPBS via Vola", e);
    }
  }

  @Override
  public Payment get(PaymentId paymentId) {
    try {
      log.info("Retrieving MPBS via Vola for student: {}", paymentId.getPspPaymentId());
      return volaClient.get(paymentId);
    } catch (Exception e) {
      log.error("Error retrieving MPBS via Vola", e);
      throw new DataRetrievalFailureException("Failed to retrieve MPBS via Vola", e);
    }
  }

  @Override
  public List<Payment> getPayments(List<PaymentId> paymentIds) {
    try {
      return volaClient.getPayments(paymentIds);
    } catch (Exception e) {
      log.error("Error retrieving multiple MPBS via Vola", e);
      throw new DataRetrievalFailureException("Failed to retrieve multiple MPBS via Vola", e);
    }
  }
}
