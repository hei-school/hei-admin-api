package school.hei.haapi.model.psp.vola.api;

import school.hei.haapi.model.psp.vola.api.gen.client.ApiClient;
import school.hei.haapi.model.psp.vola.api.gen.client.api.PaymentControllerApi;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment;

public class VolaClient {
  private final String apiKey;
  private final PaymentControllerApi paymentControllerApi;

  public VolaClient(String baseUrl, String apiKey) {
    this.apiKey = apiKey;
    var apiClient = new ApiClient();
    apiClient.setBasePath(baseUrl);
    this.paymentControllerApi = new PaymentControllerApi(apiClient);
  }

  public Payment create(PspPayment.PspTypeEnum pspType, String pspId, String email) {
    return paymentControllerApi.createPayment(apiKey, email, pspType.toString(), pspId);
  }

  public Payment get(PspPayment.PspTypeEnum pspType, String pspId, String email) {
    return paymentControllerApi.getPayment(apiKey, email, pspType.toString(), pspId);
  }
}
