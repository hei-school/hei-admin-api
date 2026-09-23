package school.hei.haapi.service.sms;

import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SmsSegmentCounter {

  private static final String GSM7_BASIC =
      "@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ"
          + " !\"#¤%&'()*+,-./0123456789:;<=>?"
          + "¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§"
          + "¿abcdefghijklmnopqrstuvwxyzäöñüà";

  private static final Set<Character> GSM7_EXTENDED =
      Set.of('\f', '^', '{', '}', '\\', '[', '~', ']', '|', '€');

  private static final int GSM7_SINGLE_SEGMENT = 160;
  private static final int GSM7_MULTI_SEGMENT = 153;
  private static final int UCS2_SINGLE_SEGMENT = 70;
  private static final int UCS2_MULTI_SEGMENT = 67;

  public int countSegments(String message) {
    if (message == null || message.isEmpty()) {
      return 1;
    }
    if (isGsm7(message)) {
      var effectiveLength = effectiveGsm7Length(message);
      return effectiveLength <= GSM7_SINGLE_SEGMENT
          ? 1
          : ceilDiv(effectiveLength, GSM7_MULTI_SEGMENT);
    }
    var length = message.codePointCount(0, message.length());
    return length <= UCS2_SINGLE_SEGMENT ? 1 : ceilDiv(length, UCS2_MULTI_SEGMENT);
  }

  private boolean isGsm7(String message) {
    return message
        .chars()
        .allMatch(c -> GSM7_BASIC.indexOf(c) >= 0 || GSM7_EXTENDED.contains((char) c));
  }

  private int effectiveGsm7Length(String message) {
    var length = 0;
    for (var i = 0; i < message.length(); i++) {
      length += GSM7_EXTENDED.contains(message.charAt(i)) ? 2 : 1;
    }
    return length;
  }

  private int ceilDiv(int value, int divisor) {
    return (value + divisor - 1) / divisor;
  }
}
