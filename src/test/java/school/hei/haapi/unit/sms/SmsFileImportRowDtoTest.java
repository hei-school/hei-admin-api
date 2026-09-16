package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import school.hei.haapi.model.dto.SmsFileImportRowDto;

class SmsFileImportRowDtoTest {

  @Test
  void strips_leading_zero() {
    assertEquals("321234567", SmsFileImportRowDto.normalizePhoneNumber("0321234567"));
  }

  @Test
  void leaves_number_without_leading_zero_untouched() {
    assertEquals("321234567", SmsFileImportRowDto.normalizePhoneNumber("321234567"));
  }

  @Test
  void strips_non_digit_characters() {
    assertEquals("321234567", SmsFileImportRowDto.normalizePhoneNumber("03 21 23 45 67"));
    assertEquals("261321234567", SmsFileImportRowDto.normalizePhoneNumber("+261 32 123 45 67"));
  }

  @Test
  void only_strips_a_single_leading_zero() {
    assertEquals("0321234567", SmsFileImportRowDto.normalizePhoneNumber("00321234567"));
  }
}
