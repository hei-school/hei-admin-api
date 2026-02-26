package school.hei.haapi.model.psp.vola.api.gen.client.model.mapper;

import static school.hei.haapi.model.psp.PspType.ORANGE_MONEY;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.psp.PspType;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PaymentId;

@Component
public class VolaMapper {

  public PspType toPspTypeEnum(MobileMoneyType mobileMoneyType) {
    if (MobileMoneyType.ORANGE_MONEY.equals(mobileMoneyType)) {
      return ORANGE_MONEY;
    }
    throw new UnsupportedOperationException("Unsupported mobileMoneyType: " + mobileMoneyType);
  }

  public MpbsStatus toMpbsStatus(Payment.VerificationStatusEnum volaStatus) {
    return switch (volaStatus) {
      case VERIFYING -> MpbsStatus.PENDING;
      case SUCCEEDED -> MpbsStatus.SUCCESS;
      case FAILED -> MpbsStatus.FAILED;
    };
  }

  public PaymentId mpbsToPaymentIds(Mpbs mpbs) {
    return PaymentId.builder()
        .pspPaymentId(mpbs.getPspId())
        .payerEmail(mpbs.getStudent().getEmail())
        .pspType(toPspTypeEnum(mpbs.getMobileMoneyType()))
        .build();
  }
}
