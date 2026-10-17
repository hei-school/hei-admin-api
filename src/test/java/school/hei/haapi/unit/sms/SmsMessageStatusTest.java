package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import school.hei.haapi.model.SmsMessageStatus;

class SmsMessageStatusTest {

  @Test
  void every_documented_befiana_status_is_recognized_case_insensitively() {
    assertEquals(SmsMessageStatus.SCHEDULED, SmsMessageStatus.fromBefiana("scheduled"));
    assertEquals(SmsMessageStatus.PROCESSING, SmsMessageStatus.fromBefiana("Processing"));
    assertEquals(SmsMessageStatus.ACCEPTED, SmsMessageStatus.fromBefiana("ACCEPTED"));
    assertEquals(SmsMessageStatus.DELIVERED, SmsMessageStatus.fromBefiana("delivered"));
    assertEquals(SmsMessageStatus.UNDELIVERABLE, SmsMessageStatus.fromBefiana("undeliverable"));
    assertEquals(SmsMessageStatus.BLOCKED, SmsMessageStatus.fromBefiana("blocked"));
    assertEquals(SmsMessageStatus.FAILED, SmsMessageStatus.fromBefiana("failed"));
    assertEquals(SmsMessageStatus.UNKNOWN, SmsMessageStatus.fromBefiana("unknown"));
  }

  @Test
  void an_unrecognized_or_missing_status_falls_back_to_unknown_instead_of_throwing() {
    assertEquals(SmsMessageStatus.UNKNOWN, SmsMessageStatus.fromBefiana("some_new_status_v3"));
    assertEquals(SmsMessageStatus.UNKNOWN, SmsMessageStatus.fromBefiana(null));
  }
}
