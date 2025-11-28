package school.hei.haapi.model.psp;

import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;

public interface Psp {
  Payment create(PspType pspType, String pspId, String email);

  Payment get(PspType pspType, String pspId, String email);
}
