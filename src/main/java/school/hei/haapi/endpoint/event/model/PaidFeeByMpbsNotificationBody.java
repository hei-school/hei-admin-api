package school.hei.haapi.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString
public class PaidFeeByMpbsNotificationBody extends PojaEvent {
  @JsonProperty("mpbs_author")
  private String mpbsAuthor;

  @JsonProperty("mpbs_author_email")
  private String mpbsAuthorEmail;

  @JsonProperty("amount")
  private int amount;

  @JsonProperty("payment_type")
  private MobileMoneyType paymentType;

  public static PaidFeeByMpbsNotificationBody from(
      Payment payment, MobileMoneyType mobileMoneyType) {
    Fee correspondingFee = payment.getFee();
    User correspondingStudent = correspondingFee.getStudent();
    return PaidFeeByMpbsNotificationBody.builder()
        .amount(payment.getAmount())
        .mpbsAuthor(extractFullNameFrom(correspondingStudent))
        .paymentType(mobileMoneyType)
        .mpbsAuthorEmail(correspondingStudent.getEmail())
        .build();
  }

  private static String extractFullNameFrom(User correspondingStudent) {
    return correspondingStudent.getLastName() + " " + correspondingStudent.getFirstName();
  }

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(1);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofMinutes(1);
  }
}
