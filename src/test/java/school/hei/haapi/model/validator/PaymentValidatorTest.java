package school.hei.haapi.model.validator;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotImplementedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.Fee.TypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.Fee.TypeEnum.TUITION;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CASH;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.MOBILE_MONEY;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.SCHOLARSHIP;


class PaymentValidatorTest {
  PaymentValidator subject;

  @BeforeEach
  void setUp() {
    subject = new PaymentValidator();
  }

  static User student1() {
    return User.builder()
        .id(TestUtils.STUDENT1_ID)
        .build();
  }

  static User student2() {
    return User.builder()
        .id(TestUtils.STUDENT2_ID)
        .build();
  }

  static Fee fee1() {
    return Fee.builder()
        .id(TestUtils.FEE1_ID)
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
        .id(TestUtils.FEE2_ID)
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
        .id(TestUtils.PAYMENT1_ID)
        .fee(fee1())
        .type(CASH)
        .amount(5000)
        .comment(null)
        .creationDatetime(Instant.now())
        .build();
  }

  static Payment payment2() {
    return Payment.builder()
        .id(TestUtils.PAYMENT2_ID)
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

  @Test
  void payments_with_multiple_fees_not_implemented() {
    String payment1FeeId = payment1().getFee().getId();
    String payment2FeeId = payment2().getFee().getId();
    List<Payment> payments = List.of(payment1(), payment2());

    assertThrows(NotImplementedException.class,
        () -> subject.accept(payments));

    assertNotEquals(payment1FeeId, payment2FeeId);
  }

  @Test
  void payments_with_unique_fee_ok() {
    String payment2FeeId = payment2().getFee().getId();
    String payment3FeeId = payment3().getFee().getId();
    List<Payment> payments = List.of(payment2(), payment3());

    subject.accept(payments);

    assertEquals(payment2FeeId, payment3FeeId);
  }
}
