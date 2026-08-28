package school.hei.haapi.unit.validator;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CASH;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.MOBILE_MONEY;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.SCHOLARSHIP;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotImplementedException;
import school.hei.haapi.model.validator.PaymentValidator;

class PaymentValidatorTest {
  /** Nothing here reaches a database: these ids only have to be stable within the class. */
  private static final String STUDENT1_ID = randomUUID().toString();

  private static final String STUDENT2_ID = randomUUID().toString();
  private static final String FEE1_ID = randomUUID().toString();
  private static final String FEE2_ID = randomUUID().toString();
  private static final String PAYMENT1_ID = randomUUID().toString();
  private static final String PAYMENT2_ID = randomUUID().toString();

  PaymentValidator subject;

  static User student1() {
    return User.builder().id(STUDENT1_ID).build();
  }

  static User student2() {
    return User.builder().id(STUDENT2_ID).build();
  }

  static Fee fee1() {
    return Fee.builder()
        .id(FEE1_ID)
        .remainingAmount(3000)
        .totalAmount(4000)
        .type(HARDWARE)
        .comment(null)
        .dueDatetime(Instant.now())
        .creationDatetime(Instant.now())
        .status(UNPAID)
        .student(student2())
        .build();
  }

  static Fee fee2() {
    return Fee.builder()
        .id(FEE2_ID)
        .totalAmount(4000)
        .remainingAmount(3000)
        .type(TUITION)
        .comment(null)
        .dueDatetime(Instant.now())
        .creationDatetime(Instant.now())
        .status(UNPAID)
        .student(student1())
        .build();
  }

  static Payment payment1() {
    return Payment.builder()
        .id(PAYMENT1_ID)
        .fee(fee1())
        .type(CASH)
        .amount(5000)
        .comment(null)
        .creationDatetime(Instant.now())
        .build();
  }

  static Payment payment2() {
    return Payment.builder()
        .id(PAYMENT2_ID)
        .fee(fee2())
        .type(SCHOLARSHIP)
        .amount(1000)
        .comment("Comment is mandatory for scholarship")
        .creationDatetime(Instant.now())
        .build();
  }

  static Payment payment3() {
    return Payment.builder()
        .id("payment3_id")
        .fee(fee2())
        .type(MOBILE_MONEY)
        .amount(1000)
        .comment("Comment is mandatory for mobile money")
        .creationDatetime(Instant.now())
        .build();
  }

  @BeforeEach
  void setUp() {
    subject = new PaymentValidator();
  }

  @Test
  void payments_with_multiple_fees_not_implemented() {
    var payment1FeeId = payment1().getFee().getId();
    var payment2FeeId = payment2().getFee().getId();
    var payments = List.of(payment1(), payment2());

    assertThrows(NotImplementedException.class, () -> subject.accept(payments));

    assertNotEquals(payment1FeeId, payment2FeeId);
  }

  @Test
  void payments_with_unique_fee_ok() {
    var payment2FeeId = payment2().getFee().getId();
    var payment3FeeId = payment3().getFee().getId();
    var payments = List.of(payment2(), payment3());

    subject.accept(payments);

    assertEquals(payment2FeeId, payment3FeeId);
  }
}
