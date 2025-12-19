package school.hei.haapi.model.psp.vola.api.gen.client.model.mapper;

import static school.hei.haapi.model.psp.PspType.ORANGE_MONEY;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsStatusHistory;
import school.hei.haapi.model.psp.PspType;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PaymentInfo;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment;

@Component
public class VolaMapper {

  public PspType toPspType(MobileMoneyType mobileMoneyType) {
    switch (mobileMoneyType) {
      case ORANGE_MONEY -> {
        return ORANGE_MONEY;
      }
      default ->
          throw new UnsupportedOperationException(
              "Unsupported mobileMoneyType: " + mobileMoneyType);
    }
  }

  public PspPayment.PspTypeEnum toPspPaymentType(PspType pspType) {
    switch (pspType) {
      case ORANGE_MONEY -> {
        return PspPayment.PspTypeEnum.ORANGE_MONEY;
      }
      default -> throw new UnsupportedOperationException("Unsupported pspType: " + pspType);
    }
  }

  public Mpbs toMpbs(Mpbs mpbs, Payment volaPayment) {
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

  public MpbsStatus toMpbsStatus(Payment.VerificationStatusEnum volaStatus) {
    return switch (volaStatus) {
      case VERIFYING -> MpbsStatus.PENDING;
      case SUCCEEDED -> MpbsStatus.SUCCESS;
      case FAILED -> MpbsStatus.FAILED;
    };
  }

  public PaymentInfo mpbsToPaymentInfos(Mpbs mpbs) {
    return PaymentInfo.builder()
        .pspPaymentId(mpbs.getPspId())
        .payerEmail(mpbs.getStudent().getEmail())
        .pspType(toPspType(mpbs.getMobileMoneyType()))
        .build();
  }
}
