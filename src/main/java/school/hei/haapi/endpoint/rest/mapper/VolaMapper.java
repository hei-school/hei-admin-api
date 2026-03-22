package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.model.psp.PspType.ORANGE_MONEY;

import java.time.Instant;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.PaymentStatus;
import school.hei.haapi.model.VolaPayment;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.psp.PspType;

public class VolaMapper {

  public PspType toPspType(MobileMoneyType mobileMoneyType) {
    switch (mobileMoneyType) {
      case ORANGE_MONEY -> {
        return ORANGE_MONEY;
      }
      default -> {
        throw new RuntimeException("PspType not supported");
      }
    }
  }

  public school.hei.haapi.endpoint.rest.model.Mpbs toRestMpbs(
      school.hei.haapi.endpoint.rest.model.Mpbs mpbs, VolaPayment volaPayment) {
    Instant successfullyVerifiedOn =
        volaPayment.status() == PaymentStatus.CONFIRMED
            ? volaPayment.pspLastVerificationInstant()
            : null;
    school.hei.haapi.endpoint.rest.model.Mpbs restMpbs =
        new school.hei.haapi.endpoint.rest.model.Mpbs();
    restMpbs.setAmount(volaPayment.amount());
    restMpbs.setCreationDatetime(volaPayment.creationInstant());
    restMpbs.setPspOwnDatetimeVerification(successfullyVerifiedOn);
    restMpbs.setSuccessfullyVerifiedOn(successfullyVerifiedOn);
    restMpbs.setLastDatetimeVerification(volaPayment.pspLastVerificationInstant());
    restMpbs.setStatus(mapPaymentStatusToMpbsStatus(volaPayment.status()));
    // remainingRetry is not present in domain model, setting to null
    restMpbs.setRemainingRetry(null);
    restMpbs.setId(mpbs.getId());
    restMpbs.setStudentId(mpbs.getStudentId());
    restMpbs.setFeeId(mpbs.getFeeId());
    restMpbs.setPspId(volaPayment.pspId());
    restMpbs.setPspType(toMobilePaymentType(volaPayment.pspType()));
    return restMpbs;
  }

  public Mpbs toMpbs(Mpbs mpbs, VolaPayment volaPayment) {
    Instant successfullyVerifiedOn =
        volaPayment.status() == PaymentStatus.CONFIRMED
            ? volaPayment.pspLastVerificationInstant()
            : null;
    return Mpbs.builder()
        .amount(volaPayment.amount())
        .successfullyVerifiedOn(successfullyVerifiedOn)
        .lastVerificationDatetime(volaPayment.pspLastVerificationInstant())
        .pspOwnDatetimeVerification(successfullyVerifiedOn)
        .student(mpbs.getStudent())
        .fee(mpbs.getFee())
        .status(mapPaymentStatusToMpbsStatus(volaPayment.status()))
        .statusHistory(mpbs.getStatusHistory())
        .id(mpbs.getId())
        .pspId(volaPayment.pspId())
        .mobileMoneyType(toMobilePaymentType(volaPayment.pspType()))
        .creationDatetime(volaPayment.creationInstant())
        .build();
  }

  private MpbsStatus mapPaymentStatusToMpbsStatus(PaymentStatus paymentStatus) {
    return switch (paymentStatus) {
      case VERIFYING -> MpbsStatus.PENDING;
      case CONFIRMED -> MpbsStatus.SUCCESS;
      case REFUSED -> MpbsStatus.FAILED;
      case UNKNOWN -> MpbsStatus.PENDING; // Default to PENDING for unknown status
    };
  }

  public MobileMoneyType toMobilePaymentType(PspType pspType) {
    switch (pspType) {
      case ORANGE_MONEY -> {
        return MobileMoneyType.ORANGE_MONEY;
      }
      default -> {
        throw new RuntimeException("PspType not supported");
      }
    }
  }
}
