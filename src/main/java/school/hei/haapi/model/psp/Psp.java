package school.hei.haapi.model.psp;

import school.hei.haapi.model.VolaPayment;

public interface Psp {
  VolaPayment create(PspType pspType, String pspId, String email);

  VolaPayment get(PspType pspType, String pspId, String email);
}
