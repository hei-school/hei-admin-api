package school.hei.haapi.model.psp.vola.api;

import java.util.List;
import school.hei.haapi.model.psp.vola.api.gen.client.ApiClient;
import school.hei.haapi.model.psp.vola.api.gen.client.api.PaymentControllerApi;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PaymentId;

public class VolaClient {
  private final String apiKey;
  private final PaymentControllerApi paymentControllerApi;

  public VolaClient(String baseUrl, String apiKey) {
    this.apiKey = apiKey;
    var apiClient = new ApiClient();
    apiClient.setBasePath(baseUrl);
    this.paymentControllerApi = new PaymentControllerApi(apiClient);
  }

  public Payment get(PaymentId paymentId) {
    return paymentControllerApi.getPayment(apiKey, paymentId);
  }

  public List<Payment> getPayments(List<PaymentId> paymentIds) {
    return paymentControllerApi.getPayments(apiKey, paymentIds);
  }
}
