package school.hei.haapi.model;

import static org.junit.jupiter.api.Assertions.*;
import static school.hei.haapi.integration.conf.MockUtils.someFee;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class FeeTest {
  @Test
  void copy_fee_correct() {
    User student = new User();
    var fee = someFee(student);
    var feeCopy = new Fee(fee);
    fee.setCreationDatetime(Instant.MIN);
    feeCopy.setCreationDatetime(Instant.MIN);

    assertEquals(fee, feeCopy);
  }
}
