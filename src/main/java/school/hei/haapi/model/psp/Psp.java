package school.hei.haapi.model.psp;

import school.hei.haapi.model.mpbs.Mpbs;

public interface Psp {
  Mpbs create(Mpbs mpbs); // PspType pspType, String pspId, String email

  Mpbs get(Mpbs mpbs); // PspType pspType, String pspId, String email
}
