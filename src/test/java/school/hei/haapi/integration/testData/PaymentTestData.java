package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.model.PaymentStatus.VALIDATE;

import java.time.Instant;
import school.hei.haapi.endpoint.rest.model.Payment.TypeEnum;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;

public class PaymentTestData {
  public static Payment aPayment(
      Fee fee, TypeEnum type, Integer amount, String comment, Instant creationDatetime) {
    return Payment.builder()
        .id(randomUUID().toString())
        .fee(fee)
        .type(type)
        .amount(amount)
        .comment(comment)
        .status(VALIDATE)
        .isDeleted(false)
        .creationDatetime(creationDatetime)
        .build();
  }
}
