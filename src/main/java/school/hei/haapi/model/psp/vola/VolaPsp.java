package school.hei.haapi.model.psp.vola;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import school.hei.haapi.model.psp.vola.api.VolaClient;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment;

@Slf4j
@AllArgsConstructor
public class VolaPsp {
  private final VolaClient volaClient;

  public Payment create(PspPayment.PspTypeEnum pspType, String pspId, String email) {
    return volaClient.create(pspType, pspId, email);
  }

  public Payment get(PspPayment.PspTypeEnum pspType, String pspId, String email) {
    return volaClient.get(pspType, pspId, email);
  }
}
