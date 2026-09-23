package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.dto.SmsFileImportRowDto;

class SmsFileImportRowDtoCellParsingTest {
  private final XSSFWorkbook workbook = new XSSFWorkbook();
  private final Row row = workbook.createSheet().createRow(0);

  private String phoneNumberFrom(org.apache.poi.ss.usermodel.Cell cell) {
    return (String) SmsFileImportRowDto.getCellMap().get("phoneNumber").mapper().apply(cell);
  }

  private String messageFrom(org.apache.poi.ss.usermodel.Cell cell) {
    return (String) SmsFileImportRowDto.getCellMap().get("message").mapper().apply(cell);
  }

  @Test
  void cell_map_reads_phone_number_from_column_zero_and_message_from_column_one() {
    var cellMap = SmsFileImportRowDto.getCellMap();

    assertEquals(0, cellMap.get("phoneNumber").colNumber());
    assertEquals(1, cellMap.get("message").colNumber());
  }

  @Test
  void phone_number_cell_as_a_string() {
    var cell = row.createCell(0);
    cell.setCellValue("0321234567");

    assertEquals("321234567", phoneNumberFrom(cell));
  }

  @Test
  void phone_number_cell_as_a_number() {
    var cell = row.createCell(0);
    cell.setCellValue(321234567);

    assertEquals("321234567", phoneNumberFrom(cell));
  }

  @Test
  void a_null_phone_number_cell_is_rejected() {
    assertThrows(IllegalArgumentException.class, () -> phoneNumberFrom(null));
  }

  @Test
  void a_blank_phone_number_cell_is_rejected() {
    var cell = row.createCell(0);

    assertThrows(IllegalArgumentException.class, () -> phoneNumberFrom(cell));
  }

  @Test
  void an_unsupported_cell_type_for_a_phone_number_is_rejected() {
    var cell = row.createCell(0);
    cell.setCellValue(true);

    assertThrows(IllegalArgumentException.class, () -> phoneNumberFrom(cell));
  }

  @Test
  void a_too_short_phone_number_is_rejected() {
    var cell = row.createCell(0);
    cell.setCellValue("12");

    assertThrows(IllegalArgumentException.class, () -> phoneNumberFrom(cell));
  }

  @Test
  void a_null_message_cell_means_the_shared_message_applies() {
    assertNull(messageFrom(null));
  }

  @Test
  void a_blank_message_cell_means_the_shared_message_applies() {
    var cell = row.createCell(1);

    assertNull(messageFrom(cell));
  }

  @Test
  void an_empty_string_message_cell_means_the_shared_message_applies() {
    var cell = row.createCell(1);
    cell.setCellValue("   ");

    assertNull(messageFrom(cell));
  }

  @Test
  void a_string_message_cell_is_its_own_personalized_message() {
    var cell = row.createCell(1);
    cell.setCellValue("Bonjour Antenaina");

    assertEquals("Bonjour Antenaina", messageFrom(cell));
  }

  @Test
  void a_non_string_message_cell_means_the_shared_message_applies() {
    var cell = row.createCell(1);
    cell.setCellValue(42);

    assertNull(messageFrom(cell));
  }
}
