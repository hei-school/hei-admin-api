package school.hei.haapi.model;

import static org.junit.jupiter.api.Assertions.*;
import static school.hei.haapi.integration.conf.MockUtils.someFee;

import org.junit.jupiter.api.Test;

class FeeTest {
  @Test
  void equals_test() {
    Fee fee1 = someFeeWithTotalAndRemainingAmount(new User(), 0, 0);
    Fee fee2 = someFeeWithTotalAndRemainingAmount(new User(), 100, 0);

    assertEquals(fee1, fee1);
    assertNotEquals(fee1, fee2);
    assertNotEquals(null, fee1);
  }

  @Test
  void hashCode_test() {
    User student = new User();
    Fee fee1 = someFee(student);
    Fee fee2 = someFee(student);

    assertEquals(fee1.hashCode(), fee1.hashCode());
    assertNotEquals(fee1.hashCode(), fee2.hashCode());
    assertNotEquals(0, fee1.hashCode());
  }

  @Test
  void copy_fee_correct() {
    User student = new User();
    var fee = someFee(student);
    var feeCopy = new Fee(fee);
    fee.setCreationDatetime(null);
    feeCopy.setCreationDatetime(null);

    assertEquals(fee, feeCopy);
  }

  private static Fee someFeeWithTotalAndRemainingAmount(
      User user, int totalAmount, int remainingAmount) {
    Fee feeResult = someFee(user);
    feeResult.setTotalAmount(totalAmount);
    feeResult.setRemainingAmount(remainingAmount);
    return feeResult;
  }
}
