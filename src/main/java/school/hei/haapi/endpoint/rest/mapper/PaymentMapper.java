package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.endpoint.rest.model.Payment;
import school.hei.haapi.endpoint.rest.validator.CreatePaymentValidator;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PaymentStatus;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.FeeService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@Component
@AllArgsConstructor
public class PaymentMapper {
  private final FeeService feeService;
  private final CreatePaymentValidator createPaymentValidator;

  public Payment toRestPayment(school.hei.haapi.model.Payment payment) {
    return new Payment()
        .id(payment.getId())
        .feeId(payment.getFee().getId())
        .type(payment.getType())
        .amount(payment.getAmount())
        .comment(payment.getComment())
            .status(school.hei.haapi.endpoint.rest.model.PaymentStatus.valueOf(payment.getStatus().toString()))
        .creationDatetime(payment.getCreationDatetime());
  }

  public List<Payment> toRestPayment(List<school.hei.haapi.model.Payment> payments){
      return payments.stream().map(this::toRestPayment).toList();
  }

  private school.hei.haapi.model.Payment toDomainPayment(
      Fee associatedFee, CreatePayment createPayment) {
    createPaymentValidator.accept(createPayment);
    return school.hei.haapi.model.Payment.builder()
        .fee(associatedFee)
        .type(toDomainPaymentType(createPayment.getType()))
        .creationDatetime(createPayment.getCreationDatetime())
        .amount(createPayment.getAmount())
        .comment(createPayment.getComment())
            .status(PaymentStatus.valueOf(createPayment.getStatus().toString()))
        .build();
  }

  public List<school.hei.haapi.model.Payment> toDomainPayment(
      String feeId, List<CreatePayment> createPayment) {
    Fee associatedFee = feeService.getById(feeId);
    if (associatedFee == null) {
      throw new NotFoundException("Fee.id=" + feeId + " is not found");
    }
    return createPayment.stream()
        .map(payment -> toDomainPayment(associatedFee, payment))
        .collect(toUnmodifiableList());
  }

  private Payment.TypeEnum toDomainPaymentType(CreatePayment.TypeEnum createPaymentType) {
    switch (createPaymentType) {
      case CASH:
        return Payment.TypeEnum.CASH;
      case SCHOLARSHIP:
        return Payment.TypeEnum.SCHOLARSHIP;
      case MOBILE_MONEY:
        return Payment.TypeEnum.MOBILE_MONEY;
      case FIX:
        return Payment.TypeEnum.FIX;
      case BANK_TRANSFER:
        return Payment.TypeEnum.BANK_TRANSFER;
        case CREDIT:
            return Payment.TypeEnum.CREDIT;
      default:
        throw new BadRequestException("Unexpected paymentType: " + createPaymentType.getValue());
    }
  }
}
