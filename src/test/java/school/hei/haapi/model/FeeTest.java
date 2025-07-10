package school.hei.haapi.model;

import static java.time.Instant.MIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;

import java.util.List;
import org.junit.jupiter.api.Test;
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
    Mpbs pendingMpbs = Mpbs.builder().status(PENDING).build();
    Mpbs failedMpbs = Mpbs.builder().status(FAILED).build();
    Mpbs successMpbs = Mpbs.builder().status(SUCCESS).build();
    var feeWithPendingMpbs =
        Fee.builder().mobilePayments(List.of(pendingMpbs, failedMpbs, successMpbs)).build();
    var feeWithoutPendingMpbs =
        Fee.builder().mobilePayments(List.of(failedMpbs, successMpbs)).build();

    assertTrue(feeWithoutPendingMpbs.haveNoPendingMobilePayments());
    assertFalse(feeWithPendingMpbs.haveNoPendingMobilePayments());
  }
}
