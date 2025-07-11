package school.hei.haapi.model;

import static java.time.Instant.MIN;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HALF_DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.integration.conf.FakeDataProvider;
import school.hei.haapi.model.Mpbs.Mpbs;

class FeeTest {
  FakeDataProvider fakeDataProvider = new FakeDataProvider();

  @Test
  void copy_fee_correct() {
    User student = new User();
    var fee = fakeDataProvider.someFee(student);
    var feeCopy = new Fee(fee);
    fee.setCreationDatetime(MIN);
    feeCopy.setCreationDatetime(MIN);

    assertEquals(fee, feeCopy);
  }

  @Test
  void check_presence_of_payment_in_fee() {
    var pendingMpbs = someMpbs(MpbsStatus.PENDING);
    var failedMpbs = someMpbs(MpbsStatus.FAILED);
    var successMpbs = someMpbs(MpbsStatus.SUCCESS);
    var feeWithPendingMpbs =
        Fee.builder().mobilePayments(List.of(pendingMpbs, failedMpbs, successMpbs)).build();
    var feeWithoutPendingMpbs =
        Fee.builder().mobilePayments(List.of(failedMpbs, successMpbs)).build();

    assertTrue(feeWithoutPendingMpbs.haveNoPendingMobilePayments());
    assertFalse(feeWithPendingMpbs.haveNoPendingMobilePayments());
  }

  @Test
  void late_fees_under_certain_conditions() {
    var pendingMpbs = someMpbs(MpbsStatus.PENDING);
    var failedMpbs = someMpbs(MpbsStatus.FAILED);
    var successMpbs = someMpbs(MpbsStatus.SUCCESS);
    var notExpiredDate = Instant.now().plus(1, HALF_DAYS);
    var expiredDate = Instant.now().minus(10, DAYS);
    var unpaidFeeMustBeLate =
        Fee.builder().dueDatetime(expiredDate).status(UNPAID).mobilePayments(List.of()).build();
    var lateFee =
        Fee.builder().dueDatetime(expiredDate).status(LATE).mobilePayments(List.of()).build();
    var paidFee =
        Fee.builder()
            .dueDatetime(notExpiredDate)
            .status(PAID)
            .mobilePayments(List.of(successMpbs))
            .build();
    var unpaidButNotLateFee =
        Fee.builder()
            .dueDatetime(notExpiredDate)
            .status(UNPAID)
            .mobilePayments(List.of(failedMpbs))
            .build();
    var pendingFee =
        Fee.builder().dueDatetime(notExpiredDate).status(PENDING).mobilePayments(List.of()).build();
    var expiredPendingFee =
        Fee.builder().dueDatetime(expiredDate).status(PENDING).mobilePayments(List.of()).build();
    var pendingFeeWithPendingPayment =
        Fee.builder()
            .dueDatetime(notExpiredDate)
            .status(PENDING)
            .mobilePayments(List.of(pendingMpbs))
            .build();

    assertFalse(paidFee.mustBeLate());
    assertFalse(unpaidButNotLateFee.mustBeLate());
    assertFalse(pendingFee.mustBeLate());
    assertFalse(pendingFeeWithPendingPayment.mustBeLate());
    assertTrue(unpaidFeeMustBeLate.mustBeLate());
    assertTrue(lateFee.mustBeLate());
    assertTrue(expiredPendingFee.mustBeLate());
  }

  private static Mpbs someMpbs(MpbsStatus pending) {
    return Mpbs.builder().status(pending).build();
  }
}
