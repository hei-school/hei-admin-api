package school.hei.haapi.model.psp.vola.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataRetrievalFailureException;
import school.hei.haapi.model.psp.Psp;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PaymentId;

@Slf4j
@AllArgsConstructor
public class VolaPsp implements Psp {
  private final VolaClient volaClient;

  @Override
  public Payment getPayment(PaymentId paymentId) {
    return getPayments(List.of(paymentId)).getFirst();
  }

  @Override
  public List<Payment> getPayments(List<PaymentId> paymentIds) {
    try {
      log.info("Retrieving {} payments via Vola - paymentIds: {}", paymentIds.size(), paymentIds);

      var paymentRetrieved = volaClient.getPayments(paymentIds);

      log.info("Successfully retrieved {} payments via Vola", paymentRetrieved.size());
      log.debug("Retrieved payments: {}", paymentRetrieved);

      return paymentRetrieved;
    } catch (Exception e) {
      log.error(
          "Error retrieving {} payments via Vola - paymentIds: {}",
          paymentIds.size(),
          paymentIds,
          e);
      throw new DataRetrievalFailureException("Failed to retrieve multiple MPBS via Vola", e);
    }
  }
}
