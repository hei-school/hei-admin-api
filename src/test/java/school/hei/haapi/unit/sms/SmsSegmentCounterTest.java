package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import school.hei.haapi.service.sms.SmsSegmentCounter;

class SmsSegmentCounterTest {
  private final SmsSegmentCounter subject = new SmsSegmentCounter();

  @Test
  void empty_or_null_message_costs_one_segment() {
    assertEquals(1, subject.countSegments(null));
    assertEquals(1, subject.countSegments(""));
  }

  @Test
  void short_gsm7_message_is_one_segment() {
    assertEquals(1, subject.countSegments("Hello Antenaina, this is a test message!"));
  }

  @Test
  void gsm7_message_of_exactly_160_chars_is_one_segment() {
    var message = "a".repeat(160);
    assertEquals(1, subject.countSegments(message));
  }

  @Test
  void gsm7_message_of_161_chars_needs_two_segments() {
    var message = "a".repeat(161);
    assertEquals(2, subject.countSegments(message));
  }

  @Test
  void gsm7_message_of_306_chars_still_fits_two_segments() {
    var message = "a".repeat(306); // 2 * 153
    assertEquals(2, subject.countSegments(message));
  }

  @Test
  void gsm7_message_of_307_chars_needs_three_segments() {
    var message = "a".repeat(307);
    assertEquals(3, subject.countSegments(message));
  }

  @Test
  void french_accents_stay_within_gsm7_single_segment() {
    // é è à ù (and uppercase É/Ç) are part of the GSM 03.38 default alphabet.
    var message = "Écoute élève, c'est déjà décidé : à partir de maintenant, c'est validé.";
    assertEquals(1, subject.countSegments(message));
  }

  @Test
  void lowercase_c_cedilla_is_not_in_gsm7_and_forces_ucs2() {
    var message = "ça va";
    assertEquals(
        1, subject.countSegments(message)); // still 1 segment, just under the 70-char UCS-2 cap
    var longMessage = "ça " + "a".repeat(68); // 71 chars total, forced into UCS-2
    assertEquals(2, subject.countSegments(longMessage));
  }

  @Test
  void message_with_emoji_forces_ucs2_encoding() {
    var message = "Bonjour 😀"; // contains an emoji, not in GSM-7 at all
    assertEquals(1, subject.countSegments(message));
  }

  @Test
  void ucs2_message_of_exactly_70_chars_is_one_segment() {
    var message = "★".repeat(70); // any non-GSM-7 char forces UCS-2
    assertEquals(1, subject.countSegments(message));
  }

  @Test
  void ucs2_message_of_71_chars_needs_two_segments() {
    var message = "★".repeat(71);
    assertEquals(2, subject.countSegments(message));
  }

  @Test
  void gsm7_extended_table_chars_count_as_two_septets() {
    var message = "€".repeat(80); // 160 septets worth, still GSM-7
    assertEquals(1, subject.countSegments(message));
    var overLimit = "€".repeat(81); // 162 septets -> over the 160 single-segment limit
    assertEquals(2, subject.countSegments(overLimit));
  }
}
