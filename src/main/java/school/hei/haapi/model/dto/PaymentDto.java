package school.hei.haapi.model.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import school.hei.haapi.endpoint.rest.model.Payment.TypeEnum;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.PaymentNumberSequence;

@Getter
@AllArgsConstructor
@Builder
public class PaymentDto {
  private String id;
  private int amount;
  private TypeEnum paymentType;
  private UserDto user;
  private FeeDto fee;
  private PaymentNumberSequence sequence;
  private Instant creationDatetime;

  public static PaymentDto from(Payment payment) {
    return PaymentDto.builder()
        .amount(payment.getAmount())
        .id(payment.getId())
        .user(UserDto.from(payment.getFee().getStudent()))
        .fee(FeeDto.from(payment.getFee()))
        .sequence(payment.getSequence())
        .creationDatetime(payment.getCreationDatetime())
        .paymentType(payment.getType())
        .build();
  }
}
