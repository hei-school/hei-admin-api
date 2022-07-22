package school.hei.haapi.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.repository.FeeRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.Fee.TypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CASH;

class FeeServiceTest {
  FeeService feeService;
  FeeRepository feeRepository;
  FeeValidator feeValidator;

  @BeforeEach
  void setUp() {
    feeRepository = mock(FeeRepository.class);
    feeValidator = mock(FeeValidator.class);
    feeService = new FeeService(feeRepository, feeValidator);
  }

  static int remainingAmount() {
    return 4000;
  }

  static User student1() {
    return User.builder()
        .id(TestUtils.STUDENT1_ID)
        .build();
  }

  static Fee fee1(int paymentAmount) {
    return Fee.builder()
        .id(TestUtils.FEE1_ID)
        .remainingAmount(remainingAmount())
        .totalAmount(remainingAmount())
        .type(HARDWARE)
        .comment(null)
        .dueDatetime(Instant.now().plus(1, ChronoUnit.DAYS))
        .creationDatetime(Instant.now())
        .status(UNPAID)
        .student(student1())
        .payments(List.of(payment1(paymentAmount)))
        .build();
  }

  static Payment payment1(int amount) {
    return Payment.builder()
        .id(TestUtils.PAYMENT1_ID)
        .type(CASH)
        .amount(amount)
        .comment(null)
        .creationDatetime(Instant.now())
        .build();
  }

  @Test
  void fee_status_is_paid() {
    Fee initial = fee1(remainingAmount());
    when(feeRepository.getById(TestUtils.FEE1_ID)).thenReturn(initial);

    Fee actual = feeService.getById(TestUtils.FEE1_ID);

    assertEquals(UNPAID, initial.getStatus());
    assertEquals(remainingAmount(), initial.getRemainingAmount());
    assertEquals(PAID, actual.getStatus());
    assertEquals(0, actual.getRemainingAmount());
  }

  @Test
  void fee_status_is_unpaid() {
    int paymentAmount = remainingAmount() - 1000;
    Fee initial = fee1(paymentAmount);
    when(feeRepository.getById(TestUtils.FEE1_ID)).thenReturn(initial);

    Fee actual = feeService.getById(TestUtils.FEE1_ID);

    assertEquals(UNPAID, actual.getStatus());
    assertNotEquals(0, actual.getRemainingAmount());
    assertTrue(actual.getDueDatetime().isAfter(Instant.now()));
  }

  @Test
  void fee_status_is_late() {
    int paymentAmount = remainingAmount() - 1000;
    Fee initial = fee1(paymentAmount);
    initial.setDueDatetime(Instant.now().minus(1L, ChronoUnit.DAYS));
    when(feeRepository.getById(TestUtils.FEE1_ID)).thenReturn(initial);

    Fee actual = feeService.getById(TestUtils.FEE1_ID);

    assertEquals(LATE, actual.getStatus());
    assertNotEquals(0, actual.getRemainingAmount());
    assertTrue(actual.getDueDatetime().isBefore(Instant.now()));
  }
}
