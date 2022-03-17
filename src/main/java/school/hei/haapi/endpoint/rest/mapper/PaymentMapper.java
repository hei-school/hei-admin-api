package school.hei.haapi.endpoint.rest.mapper;


import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.endpoint.rest.model.Payment;
import school.hei.haapi.model.Fee;
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

  private school.hei.haapi.model.Payment toDomainPayment(
      Fee associatedFee, CreatePayment createPayment) {
    if (createPayment.getAmount() == null) {
      throw new BadRequestException("Amount is mandatory");
    }
    return school.hei.haapi.model.Payment.builder()
        .fee(associatedFee)
        .type(toDomainPaymentType(createPayment.getType()))
        .amount(createPayment.getAmount())
        .comment(createPayment.getComment())
        .build();
  }

  public List<school.hei.haapi.model.Payment> toDomainPayment(
      String feeId, List<CreatePayment> createPayment) {
    Fee associatedFee = feeService.getById(feeId);
    if (associatedFee == null) {
      throw new BadRequestException("Fee.id=" + feeId + "is not found");
    }
    List<school.hei.haapi.model.Payment> payments = new ArrayList<>();
    for (CreatePayment c : createPayment) {
      payments.add(toDomainPayment(associatedFee, c));
    }
    return payments;
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
