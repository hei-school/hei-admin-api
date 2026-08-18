package school.hei.haapi.service;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.ArchiveStatusEnum.ARCHIVED;
import static school.hei.haapi.endpoint.rest.model.ArchiveStatusEnum.REJECTED;
import static school.hei.haapi.endpoint.rest.model.ArchiveStatusEnum.TO_ARCHIVE;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.YEARLY;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CASH;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.model.validator.UpdateFeeValidator;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.dao.FeeDao;
import school.hei.haapi.repository.dao.UserManagerDao;
import school.hei.haapi.repository.model.FeesStats;

class FeeServiceTest {
  /** Nothing here reaches a database: these ids only have to be stable within the class. */
  private static final String FEE1_ID = randomUUID().toString();

  private static final String FEE2_ID = randomUUID().toString();
  private static final String STUDENT1_ID = randomUUID().toString();
  private static final String PAYMENT1_ID = randomUUID().toString();

  private static FeeRepository feeRepository = mock(FeeRepository.class);
  private static UserManagerDao userManagerDao = mock(UserManagerDao.class);
  private static FeeValidator feeValidator = new FeeValidator();
  private static EventProducer eventProducer = mock(EventProducer.class);
  private static UpdateFeeValidator updateFeeValidator = mock(UpdateFeeValidator.class);
  private static FeeDao feeDao = mock(FeeDao.class);
  private static FeeTemplateService feeTemplateService = mock(FeeTemplateService.class);
  private static FeeStatusHistoryService feeStatusHistoryService =
      mock(FeeStatusHistoryService.class);
  private static BucketComponent bucketComponent = mock(BucketComponent.class);
  private static CreditService creditService = mock(CreditService.class);
  private static FeeService subject =
      new FeeService(
          feeRepository,
          feeValidator,
          updateFeeValidator,
          eventProducer,
          feeDao,
          creditService,
          feeTemplateService,
          feeStatusHistoryService,
          userManagerDao,
          bucketComponent);

  private static FeesStats emptyFeeStats() {
    return FeesStats.builder()
        .totalYearlyFees(0L)
        .totalMonthlyFees(0L)
        .totalFees(0L)
        .countOfPendingTransaction(0L)
        .countOfSuccessTransaction(0L)
        .totalLateFees(0L)
        .totalUnpaidFees(0L)
        .totalPaidFees(0L)
        .build();
  }

  static User student1() {
    return User.builder().id(STUDENT1_ID).build();
  }

  static int remainingAmount() {
    return 4000;
  }

  static Fee createSomeFee(
      String feeId,
      int paymentAmount,
      FeeStatusEnum status,
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
        .mobilePayments(List.of())
        .payments(List.of(payment1(paymentAmount, creationDatetime)))
        .build();
  }

  static Fee fee(int paymentAmount) {
    var today = Instant.now();
    var tomorrow = today.plus(1, ChronoUnit.DAYS);
    return createSomeFee(FEE1_ID, paymentAmount, UNPAID, tomorrow, today);
  }

  static Fee createMockedFee(
      boolean isMocked,
      String feeId,
      int paymentAmount,
      int remainingAmount,
      FeeStatusEnum status) {
    var dueDatetime = Instant.parse("2022-01-02T00:00:00.00Z");
    var creationDatetime = Instant.parse("2022-01-01T00:00:00.00Z");
    var fee = createSomeFee(feeId, paymentAmount, status, dueDatetime, creationDatetime);
    fee.setRemainingAmount(remainingAmount);
    if (isMocked) {
      fee = fee.toBuilder().status(UNPAID).build();
      fee.setRemainingAmount(remainingAmount());
    }
    return fee;
  }

  static Fee fee1(boolean isMocked) {
    return createMockedFee(isMocked, FEE1_ID, remainingAmount(), 0, PAID);
  }

  static Fee fee2(boolean isMocked) {
    return createMockedFee(isMocked, FEE2_ID, remainingAmount(), 0, PAID);
  }

  static Fee fee3(boolean isMocked) {
    int rest = 1;
    return createMockedFee(isMocked, FEE1_ID, remainingAmount() - rest, rest, LATE);
  }

  static Payment payment1(int amount, Instant creationDatetime) {
    return Payment.builder()
        .id(PAYMENT1_ID)
        .type(CASH)
        .amount(amount)
        .comment(null)
        .creationDatetime(creationDatetime)
        .build();
  }

  @Test
  void fee_status_is_paid_with_overpaid_mpbs() {
    var initial = fee(0);
    when(feeRepository.save(any(Fee.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

    var actual = subject.debitAmountFromMpbs(initial, 5000);

    assertEquals(PAID, actual.getStatus());
    assertEquals(0, actual.getRemainingAmount());
  }

  @Test
  void fee_status_is_paid() {
    var initial = fee(remainingAmount());
    when(feeRepository.save(any(Fee.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    when(feeRepository.findById(FEE1_ID))
        .thenReturn(Optional.of(initial.toBuilder().remainingAmount(0).status(PAID).build()));

    var actual = subject.getById(FEE1_ID);

    assertEquals(UNPAID, initial.getStatus());
    assertEquals(remainingAmount(), initial.getRemainingAmount());
    assertEquals(PAID, actual.getStatus());
    assertEquals(0, actual.getRemainingAmount());
  }

  @Test
  void fee_status_is_unpaid() {
    int rest = 1000;
    int paymentAmount = remainingAmount() - rest;
    var initial = fee(paymentAmount);
    when(feeRepository.save(any(Fee.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    when(feeRepository.findById(FEE1_ID))
        .thenReturn(
            Optional.of(
                initial.toBuilder()
                    .remainingAmount(remainingAmount() - paymentAmount)
                    .status(UNPAID)
                    .build()));

    var actual = subject.getById(FEE1_ID);

    assertEquals(UNPAID, actual.getStatus());
    assertEquals(rest, actual.getRemainingAmount());
    assertTrue(actual.getDueDatetime().isAfter(Instant.now()));
  }

  @Test
  void fee_status_is_late() {
    int rest = 1000;
    int paymentAmount = remainingAmount() - rest;
    var initial = fee(paymentAmount);
    var yesterday = Instant.now().minus(1L, ChronoUnit.DAYS);
    initial.setDueDatetime(yesterday);
    when(feeRepository.save(any(Fee.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    when(feeRepository.findById(FEE1_ID))
        .thenReturn(
            Optional.of(
                initial.toBuilder()
                    .remainingAmount(remainingAmount() - paymentAmount)
                    .status(LATE)
                    .build()));

    var actual = subject.getById(FEE1_ID);

    assertEquals(LATE, actual.getStatus());
    assertEquals(rest, actual.getRemainingAmount());
    assertTrue(actual.getDueDatetime().isBefore(Instant.now()));
  }

  @Test
  void fee_stats_handle_null_data_ok() {
    when(feeDao.getStatByCriteria(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(emptyList());
    var actual = subject.getFeesStats(null, null, null, null, null, null, false, null);
    assertEquals(emptyFeeStats(), actual);
  }

  @Test
  void update_fee_status_to_late_ok() {
    when(feeRepository.save(any(Fee.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    when(feeRepository.getUnpaidFees(any()))
        .thenReturn(IntStream.range(0, 10).mapToObj(ignored -> mockFee()).toList());
    var result = subject.updateFeesStatusToLate();
    assertTrue(result.stream().allMatch(e -> LATE.equals(e.getStatus())));
  }

  @Test
  void fees_by_status_with_exceeded_page() {
    var page1 = new PageFromOne(1);
    var page2 = new PageFromOne(2);
    var pageSize = new BoundedPageSize(10);
    boolean isMocked = true;
    when(feeRepository.findAll())
        .thenReturn(List.of(fee1(isMocked), fee2(isMocked), fee3(isMocked)));

    var actualPaidPage1 =
        subject.getFees(page1, pageSize, null, null, PAID, null, null, null, false, null);
    var actualLatePage1 =
        subject.getFees(page1, pageSize, null, null, LATE, null, null, null, false, null);
    var actualLatePage2 =
        subject.getFees(page2, pageSize, null, null, LATE, null, null, null, false, null);

    assertEquals(0, actualPaidPage1.size());
    assertEquals(0, actualLatePage1.size());
    assertEquals(0, actualLatePage2.size());
    assertFalse(actualPaidPage1.contains(fee1(!isMocked)));
    assertFalse(actualPaidPage1.contains(fee2(!isMocked)));
    assertFalse(actualLatePage1.contains(fee3(!isMocked)));
  }

  @Test
  void save_fee_without_student_ko() {
    var feesWithoutStudent = List.of(fee(100));
    feesWithoutStudent.forEach(fee -> fee.setStudent(null));

    var badRequestException =
        assertThrows(BadRequestException.class, () -> subject.saveAll(feesWithoutStudent));
    assertEquals("Student is mandatory", badRequestException.getMessage());
  }

  @Test
  void fees_by_category_with_exceeded_page() {
    var page1 = new PageFromOne(1);
    var page2 = new PageFromOne(2);
    var pageSize = new BoundedPageSize(10);
    boolean isMocked = true;
    when(feeRepository.findAll())
        .thenReturn(List.of(fee1(isMocked), fee2(isMocked), fee3(isMocked)));

    var actualPaidPage1 =
        subject.getFees(page1, pageSize, null, null, null, L1, null, null, false, null);
    var actualLatePage1 =
        subject.getFees(page1, pageSize, null, null, null, L1, null, null, false, null);
    var actualLatePage2 =
        subject.getFees(page2, pageSize, null, null, null, L1, null, null, false, null);

    assertEquals(0, actualPaidPage1.size());
    assertEquals(0, actualLatePage1.size());
    assertEquals(0, actualLatePage2.size());
    assertFalse(actualPaidPage1.contains(fee1(!isMocked)));
    assertFalse(actualPaidPage1.contains(fee2(!isMocked)));
    assertFalse(actualLatePage1.contains(fee3(!isMocked)));
  }

  @Test
  void request_archive_fee_sets_to_archive_and_notifies_validators() {
    var initial = fee(0);
    when(feeRepository.save(any(Fee.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

    var actual = subject.requestArchiveFee(initial);

    assertEquals(TO_ARCHIVE, actual.getArchiveStatus());
    assertFalse(actual.isArchived());
    verify(eventProducer, atLeastOnce()).accept(any());
  }

  @Test
  void request_archive_fee_twice_ko() {
    var initial = fee(0);
    initial.requestArchive();

    assertThrows(BadRequestException.class, () -> subject.requestArchiveFee(initial));
  }

  @Test
  void validate_archive_fee_ok() {
    var initial = fee(0);
    initial.requestArchive();
    var validator = mockUser();
    when(feeRepository.save(any(Fee.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

    Fee actual;
    try (var mockedAuthProvider = mockStatic(AuthProvider.class)) {
      mockedAuthProvider
          .when(AuthProvider::getPrincipal)
          .thenReturn(new Principal(validator, "dummy"));
      actual = subject.updateArchiveStatus(initial, ARCHIVED);
    }

    assertEquals(ARCHIVED, actual.getArchiveStatus());
    assertTrue(actual.isArchived());
    assertEquals(validator, actual.getArchivedBy());
    verify(creditService).depositArchivedFee(initial);
  }

  @Test
  void reject_archive_fee_ok() {
    var initial = fee(0);
    initial.requestArchive();
    when(feeRepository.save(any(Fee.class)))
        .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

    var actual = subject.updateArchiveStatus(initial, REJECTED);

    assertEquals(REJECTED, actual.getArchiveStatus());
    assertFalse(actual.isArchived());
  }

  @Test
  void update_archive_status_to_to_archive_ko() {
    var initial = fee(0);
    initial.requestArchive();

    assertThrows(BadRequestException.class, () -> subject.updateArchiveStatus(initial, TO_ARCHIVE));
  }

  @Test
  void validate_archive_without_prior_request_ko() {
    var initial = fee(0);

    assertThrows(BadRequestException.class, () -> subject.updateArchiveStatus(initial, ARCHIVED));
  }

  private static User mockUser() {
    return User.builder().id(STUDENT1_ID).build();
  }

  private static Fee mockFee() {
    return Fee.builder()
        .student(mockUser())
        .category(L1)
        .status(UNPAID)
        .dueDatetime(now())
        .comment("Dummy comment")
        .dueDatetime(Instant.now().minus(30, DAYS))
        .remainingAmount(100)
        .totalAmount(100)
        .frequency(YEARLY)
        .type(TUITION)
        .build();
  }
}
