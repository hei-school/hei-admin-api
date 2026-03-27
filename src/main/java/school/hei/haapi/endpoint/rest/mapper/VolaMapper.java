package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.SUCCEEDED;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.UnsupportedPspTypeException;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.mpbs.MpbsStatusHistory;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment;

@Component
public class VolaMapper {
  public PspPayment.PspTypeEnum toPspType(MobileMoneyType mobileMoneyType) {
    if (mobileMoneyType == MobileMoneyType.ORANGE_MONEY) {
      return PspPayment.PspTypeEnum.ORANGE_MONEY;
    }
    throw new UnsupportedPspTypeException(
        "PspType not supported for mobile money type: " + mobileMoneyType);
  }

  public Mpbs toMpbs(
      Payment volaPayment,
      String id,
      User student,
      Fee fee,
      List<MpbsStatusHistory> statusHistory) {
    var pspPayment = volaPayment.getPspPayment();
    var lastVerificationInstant =
        volaPayment.getLastPspVerificationInstant() != null
            ? volaPayment.getLastPspVerificationInstant().toInstant()
            : null;
    var creationInstant =
        volaPayment.getCreationInstant() != null
            ? volaPayment.getCreationInstant().toInstant()
            : null;
    Instant successfullyVerifiedOn =
        volaPayment.getVerificationStatus() == SUCCEEDED ? lastVerificationInstant : null;

    var builder =
        Mpbs.builder()
            .successfullyVerifiedOn(successfullyVerifiedOn)
            .lastVerificationDatetime(lastVerificationInstant)
            .pspOwnDatetimeVerification(successfullyVerifiedOn)
            .student(student)
            .fee(fee)
            .status(mapPaymentStatusToMpbsStatus(volaPayment.getVerificationStatus()))
            .statusHistory(statusHistory)
            .id(id)
            .creationDatetime(creationInstant);

    if (pspPayment != null) {
      builder
          .amount(pspPayment.getAmount())
          .pspId(pspPayment.getId())
          .mobileMoneyType(toMobilePaymentType(pspPayment.getPspType()));
    }

    return builder.build();
  }

  private MpbsStatus mapPaymentStatusToMpbsStatus(Payment.VerificationStatusEnum paymentStatus) {
    return switch (paymentStatus) {
      case VERIFYING -> MpbsStatus.PENDING;
      case SUCCEEDED -> MpbsStatus.SUCCESS;
      case FAILED -> MpbsStatus.FAILED;
    };
  }

  public MobileMoneyType toMobilePaymentType(PspPayment.PspTypeEnum pspType) {
    if (pspType == PspPayment.PspTypeEnum.ORANGE_MONEY) {
      return MobileMoneyType.ORANGE_MONEY;
    }
    throw new UnsupportedPspTypeException("PspType not supported for PSP type: " + pspType);
  }
}
