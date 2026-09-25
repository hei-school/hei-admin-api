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
    var messageOfTwoFullSegments = "a".repeat(306);
    assertEquals(2, subject.countSegments(messageOfTwoFullSegments));
  }

  @Test
  void gsm7_message_of_307_chars_needs_three_segments() {
    var message = "a".repeat(307);
    assertEquals(3, subject.countSegments(message));
  }

  @Test
  void french_accents_stay_within_gsm7_single_segment() {
    var messageWithGsm7Accents =
        "Écoute élève, c'est déjà décidé : à partir de maintenant, c'est validé.";
    assertEquals(1, subject.countSegments(messageWithGsm7Accents));
  }

  @Test
  void lowercase_c_cedilla_is_not_in_gsm7_and_forces_ucs2() {
    var shortMessageUnderUcs2SingleSegmentCap = "ça va";
    assertEquals(1, subject.countSegments(shortMessageUnderUcs2SingleSegmentCap));
    var messageOf71CharsForcedIntoUcs2 = "ça " + "a".repeat(68);
    assertEquals(2, subject.countSegments(messageOf71CharsForcedIntoUcs2));
  }

  @Test
  void message_with_emoji_forces_ucs2_encoding() {
    var messageWithEmojiNotInGsm7 = "Bonjour 😀";
    assertEquals(1, subject.countSegments(messageWithEmojiNotInGsm7));
  }

  @Test
  void ucs2_message_of_exactly_70_chars_is_one_segment() {
    var nonGsm7CharForcingUcs2 = "★".repeat(70);
    assertEquals(1, subject.countSegments(nonGsm7CharForcingUcs2));
  }

  @Test
  void ucs2_message_of_71_chars_needs_two_segments() {
    var message = "★".repeat(71);
    assertEquals(2, subject.countSegments(message));
  }

  @Test
  void gsm7_extended_table_chars_count_as_two_septets() {
    var messageOf160SeptetsStillGsm7 = "€".repeat(80);
    assertEquals(1, subject.countSegments(messageOf160SeptetsStillGsm7));
    var messageOf162SeptetsOverSingleSegmentLimit = "€".repeat(81);
    assertEquals(2, subject.countSegments(messageOf162SeptetsOverSingleSegmentLimit));
  }
}
