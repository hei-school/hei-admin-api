package school.hei.haapi.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.repository.FeeRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.Fee.TypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CASH;

class FeeServiceTest {
  FeeService subject;
  FeeRepository feeRepository;
  FeeValidator feeValidator;

  @BeforeEach
  void setUp() {
    feeRepository = mock(FeeRepository.class);
    feeValidator = mock(FeeValidator.class);
    subject = new FeeService(feeRepository, feeValidator);
  }

  static User student1() {
    return User.builder()
        .id(TestUtils.STUDENT1_ID)
        .build();
  }

  static int remainingAmount() {
    return 4000;
  }

  static Fee createSomeFee(
      String feeId,
      int paymentAmount,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status,
      Instant dueDatetime,
      Instant creationDatetime) {
    return Fee.builder()
        .id(feeId)
        .remainingAmount(remainingAmount())
        .totalAmount(remainingAmount())
        .type(HARDWARE)
        .comment(null)
        .dueDatetime(dueDatetime)
        .creationDatetime(creationDatetime)
        .status(status)
        .student(student1())
        .payments(List.of(payment1(paymentAmount, creationDatetime)))
        .build();
  }

  static Fee fee(int paymentAmount) {
    Instant today = Instant.now();
    Instant tomorrow = today.plus(1, ChronoUnit.DAYS);
    return createSomeFee(TestUtils.FEE1_ID, paymentAmount, UNPAID, tomorrow, today);
  }

  static Fee createMockedFee(
      boolean isMocked,
      String feeId,
      int paymentAmount,
      int remainingAmount,
      school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
    Instant dueDatetime = Instant.parse("2022-01-02T00:00:00.00Z");
    Instant creationDatetime = Instant.parse("2022-01-01T00:00:00.00Z");
    Fee fee = createSomeFee(feeId, paymentAmount, status, dueDatetime, creationDatetime);
    fee.setRemainingAmount(remainingAmount);
    if (isMocked) {
      fee.setStatus(UNPAID);
      fee.setRemainingAmount(remainingAmount());
    }
    return fee;
  }

  static Fee fee1(boolean isMocked) {
    return createMockedFee(isMocked, TestUtils.FEE1_ID, remainingAmount(), 0, PAID);
  }

  static Fee fee2(boolean isMocked) {
    return createMockedFee(isMocked, TestUtils.FEE2_ID, remainingAmount(), 0, PAID);
  }

  static Fee fee3(boolean isMocked) {
    int rest = 1;
    return createMockedFee(isMocked, TestUtils.FEE1_ID, remainingAmount() - rest, rest, LATE);
  }

  static Payment payment1(int amount, Instant creationDatetime) {
    return Payment.builder()
        .id(TestUtils.PAYMENT1_ID)
        .type(CASH)
        .amount(amount)
        .comment(null)
        .creationDatetime(creationDatetime)
        .build();
  }

  @Test
  void fee_status_is_paid() {
    Fee initial = fee(remainingAmount());
    when(feeRepository.getById(TestUtils.FEE1_ID)).thenReturn(initial);

    Fee actual = subject.getById(TestUtils.FEE1_ID);

    assertEquals(UNPAID, initial.getStatus());
    assertEquals(remainingAmount(), initial.getRemainingAmount());
    assertEquals(PAID, actual.getStatus());
    assertEquals(0, actual.getRemainingAmount());
  }

  @Test
  void fee_status_is_unpaid() {
    int rest = 1000;
    int paymentAmount = remainingAmount() - rest;
    Fee initial = fee(paymentAmount);
    when(feeRepository.getById(TestUtils.FEE1_ID)).thenReturn(initial);

    Fee actual = subject.getById(TestUtils.FEE1_ID);

    assertEquals(UNPAID, actual.getStatus());
    assertEquals(rest, actual.getRemainingAmount());
    assertTrue(actual.getDueDatetime().isAfter(Instant.now()));
  }

  @Test
  void fee_status_is_late() {
    int rest = 1000;
    int paymentAmount = remainingAmount() - rest;
    Fee initial = fee(paymentAmount);
    Instant yesterday = Instant.now().minus(1L, ChronoUnit.DAYS);
    initial.setDueDatetime(yesterday);
    when(feeRepository.getById(TestUtils.FEE1_ID)).thenReturn(initial);

    Fee actual = subject.getById(TestUtils.FEE1_ID);

    assertEquals(LATE, actual.getStatus());
    assertEquals(rest, actual.getRemainingAmount());
    assertTrue(actual.getDueDatetime().isBefore(Instant.now()));
  }

  @Test
  void fees_by_status_with_exceeded_page() {
    PageFromOne page1 = new PageFromOne(1);
    PageFromOne page2 = new PageFromOne(2);
    BoundedPageSize pageSize = new BoundedPageSize(10);
    boolean isMocked = true;
    when(feeRepository.findAll())
        .thenReturn(List.of(
            fee1(isMocked),
            fee2(isMocked),
            fee3(isMocked)
        ));

    List<Fee> actualPaidPage1 = subject.getFees(page1, pageSize,
        PAID);
    List<Fee> actualLatePage1 = subject.getFees(page1, pageSize,
        LATE);
    List<Fee> actualLatePage2 = subject.getFees(page2, pageSize,
        LATE);

    assertEquals(2, actualPaidPage1.size());
    assertEquals(1, actualLatePage1.size());
    assertEquals(0, actualLatePage2.size());
    assertTrue(actualPaidPage1.contains(fee1(!isMocked)));
    assertTrue(actualPaidPage1.contains(fee2(!isMocked)));
    assertTrue(actualLatePage1.contains(fee3(!isMocked)));
  }
}
