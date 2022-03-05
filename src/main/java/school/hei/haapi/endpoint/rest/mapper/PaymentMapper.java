package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.endpoint.rest.model.Payment;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class PaymentMapper {
  public Payment toRestPayment(school.hei.haapi.model.Payment payment) {
    return new Payment()
        .id(payment.getId())
        .feeId(payment.getFeeId())
        .type(payment.getType())
        .amount(payment.getAmount())
        .comment(payment.getComment())
        .creationDatetime(payment.getCreationDatetime());
  }

  public school.hei.haapi.model.Payment toDomainPayment(CreatePayment createPayment) {
    return school.hei.haapi.model.Payment.builder()
        .feeId(createPayment.getFeeId())
        .type(toDomainPaymentType(createPayment.getType()))
        .amount(createPayment.getAmount())
        .comment(createPayment.getComment())
        .build();
  }

  private Payment.TypeEnum toDomainPaymentType(CreatePayment.TypeEnum createPaymentType) {
    String feeType = createPaymentType.toString();
    if (feeType.equals(Payment.TypeEnum.CASH.toString())) {
      return Payment.TypeEnum.CASH;
    } else if (feeType.equals(Payment.TypeEnum.SCOLARSHIP.toString())) {
      return Payment.TypeEnum.SCOLARSHIP;
    } else if (feeType.equals(Payment.TypeEnum.FIX.toString())) {
      return Payment.TypeEnum.FIX;
    }
    throw new BadRequestException("Payment type must be valid");
  }
}
