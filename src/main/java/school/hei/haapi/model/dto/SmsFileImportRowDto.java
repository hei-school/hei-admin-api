package school.hei.haapi.model.dto;

import static org.apache.poi.ss.usermodel.CellType.BLANK;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import school.hei.haapi.service.utils.excel.CellMap;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class SmsFileImportRowDto implements Serializable {
  private String phoneNumber;
  private String message;

  private static String getPhoneNumberFromCell(Cell cell) {
    if (cell == null || cell.getCellType() == BLANK) {
      throw new IllegalArgumentException(
          "Ligne %d ignorée : numéro manquant".formatted(cell == null ? -1 : cell.getRowIndex()));
    }
    String raw =
        switch (cell.getCellType()) {
          case STRING -> cell.getStringCellValue().trim();
          case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
          default ->
              throw new IllegalArgumentException(
                  "Ligne %d ignorée : format de numéro invalide".formatted(cell.getRowIndex()));
        };
    var normalized = normalizePhoneNumber(raw);
    if (normalized.length() < 6) {
      throw new IllegalArgumentException(
          "Ligne %d ignorée : numéro invalide".formatted(cell.getRowIndex()));
    }
    return normalized;
  }

  public static String normalizePhoneNumber(String raw) {
    var digitsOnly = raw.replaceAll("[^0-9]", "");
    return digitsOnly.startsWith("0") ? digitsOnly.substring(1) : digitsOnly;
  }

  private static String getMessageFromCell(Cell cell) {
    if (cell == null || cell.getCellType() == BLANK) {
      return null;
    }
    return switch (cell.getCellType()) {
      case STRING -> {
        var value = cell.getStringCellValue().trim();
        yield value.isEmpty() ? null : value;
      }
      default -> null;
    };
  }

  public static Map<String, CellMap<?>> getCellMap() {
    return Map.ofEntries(
        Map.entry(
            "phoneNumber", new CellMap<String>(0, SmsFileImportRowDto::getPhoneNumberFromCell)),
        Map.entry("message", new CellMap<String>(1, SmsFileImportRowDto::getMessageFromCell)));
  }
}
