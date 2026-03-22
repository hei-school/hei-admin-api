package school.hei.haapi.model.psp.vola;

import static school.hei.haapi.model.psp.PspType.ORANGE_MONEY;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import school.hei.haapi.model.PaymentStatus;
import school.hei.haapi.model.VolaPayment;
import school.hei.haapi.model.psp.Psp;
import school.hei.haapi.model.psp.PspType;
import school.hei.haapi.model.psp.vola.api.VolaClient;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PspPayment;

@Slf4j
@AllArgsConstructor
public class VolaPsp implements Psp {
  private final VolaClient volaClient;

  @Override
  public VolaPayment create(PspType pspType, String pspId, String email) {
    var volaPayment = volaClient.create(pspType, pspId, email);
    return toPayment(volaPayment);
  }

  @Override
  public VolaPayment get(PspType pspType, String pspId, String email) {
    var volaPayment = volaClient.get(pspType, pspId, email);
    return toPayment(volaPayment);
  }

  private VolaPayment toPayment(
      school.hei.haapi.model.psp.vola.api.gen.client.model.Payment volaPayment) {
    var volaPspPayment = volaPayment.getPspPayment();

    var lastVerificationInstant =
        volaPayment.getLastPspVerificationInstant() != null
            ? volaPayment.getLastPspVerificationInstant().toInstant()
            : null;

    var creationInstant =
        volaPspPayment == null || volaPspPayment.getCreationInstant() == null
            ? null
            : volaPspPayment.getCreationInstant().toInstant();

    var status = toPaymentStatus(volaPayment.getVerificationStatus());

    return volaPspPayment == null
        ? VolaPayment.builder()
            .status(status)
            .pspLastVerificationInstant(lastVerificationInstant)
            .creationInstant(null)
            .build()
        : VolaPayment.builder()
            .amount(volaPspPayment.getAmount())
            .pspType(toPspType(volaPspPayment.getPspType()))
            .pspId(volaPspPayment.getId())
            .status(status)
            .pspLastVerificationInstant(lastVerificationInstant)
            .creationInstant(creationInstant)
            .build();
  }

  private PspType toPspType(PspPayment.PspTypeEnum volaPspType) {
    return switch (volaPspType) {
      case ORANGE_MONEY -> ORANGE_MONEY;
    };
  }

  private PaymentStatus toPaymentStatus(
      school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum
          volaPaymentStatus) {
    if (volaPaymentStatus == null) {
      return PaymentStatus.UNKNOWN;
    }

    return switch (volaPaymentStatus) {
      case VERIFYING -> PaymentStatus.VERIFYING;
      case SUCCEEDED -> PaymentStatus.CONFIRMED;
      case FAILED -> PaymentStatus.REFUSED;
    };
  }
}
