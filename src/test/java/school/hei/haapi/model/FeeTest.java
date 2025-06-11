package school.hei.haapi.model;

import static java.time.Instant.MIN;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import school.hei.haapi.integration.conf.MockUtils;

class FeeTest {
  MockUtils mockUtils = new MockUtils();

  @Test
  void copy_fee_correct() {
    User student = new User();
    var fee = mockUtils.someFee(student);
    var feeCopy = new Fee(fee);
    fee.setCreationDatetime(MIN);
    feeCopy.setCreationDatetime(MIN);

    assertEquals(fee, feeCopy);
  }
}
