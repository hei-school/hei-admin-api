package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.endpoint.rest.model.Payment;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.FeeService;

@Component
@AllArgsConstructor
public class PaymentMapper {
  private final FeeService feeService;

  public Payment toRestPayment(school.hei.haapi.model.Payment payment) {
    return new Payment()
        .id(payment.getId())
        .feeId(payment.getFee().getId())
        .type(payment.getType())
        .amount(payment.getAmount())
        .comment(payment.getComment())
        .creationDatetime(payment.getCreationDatetime());
  }

  public school.hei.haapi.model.Payment toDomainPayment(CreatePayment createPayment) {
    if (createPayment.getAmount() == null) {
      throw new BadRequestException("Amount is mandatory");
    }
    return school.hei.haapi.model.Payment.builder()
        .fee(feeService.getById(createPayment.getFeeId()))
        .type(toDomainPaymentType(createPayment.getType()))
        .amount(createPayment.getAmount())
        .comment(createPayment.getComment())
        .build();
  }

  private Payment.TypeEnum toDomainPaymentType(CreatePayment.TypeEnum createPaymentType) {
    String paymentType = createPaymentType.toString();
    if (paymentType.equals(Payment.TypeEnum.CASH.toString())) {
      return Payment.TypeEnum.CASH;
    } else if (paymentType.equals(Payment.TypeEnum.SCOLARSHIP.toString())) {
      return Payment.TypeEnum.SCOLARSHIP;
    } else if (paymentType.equals(Payment.TypeEnum.FIX.toString())) {
      return Payment.TypeEnum.FIX;
    }
    throw new BadRequestException("Unexpected paymentFee: " + paymentType);
  }
}
