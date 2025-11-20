package school.hei.haapi.model.psp.vola.api;

import static school.hei.haapi.model.psp.PspType.ORANGE_MONEY;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.psp.Psp;
import school.hei.haapi.model.psp.PspType;

@Slf4j
@AllArgsConstructor
public class VolaPsp implements Psp {
  private final VolaClient volaClient;

  @Override
  public Mpbs create(Mpbs mpbs) {
    try {
      log.info("Creating MPBS via Vola for student: {}", mpbs.getStudent().getEmail());
      volaClient.create(
          toPspType(mpbs.getMobileMoneyType()), mpbs.getPspId(), mpbs.getStudent().getEmail());
      log.info("MPBS created successfully");
      return mpbs;
    } catch (Exception e) {
      log.error("Error creating MPBS via Vola", e);
      throw new RuntimeException("Failed to create MPBS via Vola", e);
    }
  }

  @Override
  public Mpbs get(Mpbs mpbs) {
    try {
      log.info("Retrieving MPBS via Vola for student: {}", mpbs.getStudent().getEmail());
      volaClient.get(
          toPspType(mpbs.getMobileMoneyType()), mpbs.getPspId(), mpbs.getStudent().getEmail());
      log.info("MPBS retrieved successfully");
      return mpbs;
    } catch (Exception e) {
      log.error("Error retrieving MPBS via Vola", e);
      throw new RuntimeException("Failed to retrieve MPBS via Vola", e);
    }
  }

  private PspType toPspType(MobileMoneyType mobileMoneyType) {
    switch (mobileMoneyType) {
      case ORANGE_MONEY -> {
        return ORANGE_MONEY;
      }
      default ->
          throw new UnsupportedOperationException(
              "ORANGE_MONEY is the only MobileMoneyType supported by Vola currently");
    }
  }
}
