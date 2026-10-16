package school.hei.haapi.model;

import java.util.Locale;

public enum SmsMessageStatus {
  SCHEDULED,
  PROCESSING,
  ACCEPTED,
  DELIVERED,
  UNDELIVERABLE,
  BLOCKED,
  FAILED,
  UNKNOWN;

  public static SmsMessageStatus fromBefiana(String rawStatus) {
    return valueOf(rawStatus.toUpperCase(Locale.ROOT));
  }
}
