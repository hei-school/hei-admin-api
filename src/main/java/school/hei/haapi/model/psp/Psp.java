package school.hei.haapi.model.psp;

import school.hei.haapi.model.mpbs.Mpbs;

public interface Psp {
  Mpbs create(Mpbs mpbs);

  Mpbs get(Mpbs mpbs);
}
