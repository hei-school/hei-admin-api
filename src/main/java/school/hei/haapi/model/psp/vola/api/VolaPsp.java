package school.hei.haapi.model.psp.vola.api;

import static school.hei.haapi.model.psp.PspType.ORANGE_MONEY;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataRetrievalFailureException;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsStatusHistory;
import school.hei.haapi.model.psp.Psp;
import school.hei.haapi.model.psp.PspType;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;

@Slf4j
@AllArgsConstructor
public class VolaPsp implements Psp {
  private final VolaClient volaClient;

  @Override
  public Mpbs create(Mpbs mpbs) {
    try {
      log.info("Creating MPBS via Vola for student: {}", mpbs.getStudent().getEmail());
      var volaPayment =
          volaClient.create(
              toPspType(mpbs.getMobileMoneyType()), mpbs.getPspId(), mpbs.getStudent().getEmail());
      log.info("MPBS created successfully");
      return toMpbs(mpbs, volaPayment);
    } catch (Exception e) {
      log.error("Error creating MPBS via Vola", e);
      throw new DataRetrievalFailureException("Failed to create MPBS via Vola", e);
    }
  }

  @Override
  public Mpbs get(Mpbs mpbs) {
    try {
      log.info("Retrieving MPBS via Vola for student: {}", mpbs.getStudent().getEmail());
      var volaPayment =
          volaClient.get(
              toPspType(mpbs.getMobileMoneyType()), mpbs.getPspId(), mpbs.getStudent().getEmail());
      log.info("MPBS retrieved successfully");

      return toMpbs(mpbs, volaPayment);
    } catch (Exception e) {
      log.error("Error retrieving MPBS via Vola", e);
      throw new DataRetrievalFailureException("Failed to retrieve MPBS via Vola", e);
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

  private Mpbs toMpbs(Mpbs mpbs, Payment volaPayment) {

    var statusHistory = mpbs.getStatusHistory();
    statusHistory.add(
        MpbsStatusHistory.builder()
            .mpbs(mpbs)
            .status(toMpbsStatus(volaPayment.getVerificationStatus()))
            .creationInstant(volaPayment.getLastPspVerificationInstant().toInstant())
            .updateInstant(volaPayment.getLastPspVerificationInstant().toInstant())
            .build());

    return Mpbs.builder()
        .id(mpbs.getId())
        .pspId(volaPayment.getPspPayment().getId())
        .amount(volaPayment.getPspPayment().getAmount())
        .lastVerificationDatetime(volaPayment.getLastPspVerificationInstant().toInstant())
        .pspOwnDatetimeVerification(mpbs.getPspOwnDatetimeVerification())
        .student(mpbs.getStudent())
        .fee(mpbs.getFee())
        .statusHistory(statusHistory)
        .status(toMpbsStatus(volaPayment.getVerificationStatus()))
        .creationDatetime(volaPayment.getPspPayment().getCreationInstant().toInstant())
        .build();
  }

  private MpbsStatus toMpbsStatus(VerificationStatusEnum volaStatus) {
    return switch (volaStatus) {
      case VERIFYING -> MpbsStatus.PENDING;
      case SUCCEEDED -> MpbsStatus.SUCCESS;
      case FAILED -> MpbsStatus.FAILED;
    };
  }
}
