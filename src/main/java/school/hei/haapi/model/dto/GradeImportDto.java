package school.hei.haapi.model.dto;

import static java.util.Map.entry;
import static org.apache.poi.ss.usermodel.CellType.BLANK;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import school.hei.haapi.service.utils.excel.CellMap;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class GradeImportDto implements Serializable {
  private String ref;
  private Double score;

  private static final String REF_REGEX = "^STD[\\w-]+$";

  private static String getRefFromCell(Cell cell) {
    verifyEmptyCell(cell);
    return validateRefCell(cell);
  }

  private static Double getScoreFromCell(Cell cell) {
    try {
      verifyEmptyCell(cell);
      return switch (cell.getCellType()) {
        case NUMERIC -> cell.getNumericCellValue();
        case STRING -> Double.parseDouble(cell.getStringCellValue().trim());
        case FORMULA ->
            switch (cell.getCachedFormulaResultType()) {
              case NUMERIC -> cell.getNumericCellValue();
              case STRING -> Double.parseDouble(cell.getStringCellValue().trim());
              default -> throw new IllegalStateException("Type non supporté en formule");
            };
        default ->
            throw new IllegalStateException("Type de cellule non supporté: " + cell.getCellType());
      };
    } catch (NumberFormatException e) {
      throw new IllegalStateException(
          "ligne %d ignorée en raison d'une valeur incovertible en décimal"
              .formatted(cell.getRowIndex()));
    }
  }

  private static void verifyEmptyCell(Cell cell) {
    if (cell == null || cell.getCellType() == BLANK) {
      throw new IllegalArgumentException(
          "Ligne %d ignorée en raison d'un champ obligatoire vide".formatted(cell.getRowIndex()));
    }
  }

  private static String validateRefCell(Cell cell) {
    var ref = cell.getStringCellValue().trim();
    if (!ref.matches(REF_REGEX)) {
      throw new IllegalArgumentException(
          "Ligne %d ignorée en raison d'un format de référence étudiant invalide"
              .formatted(cell.getRowIndex()));
    }
    return ref;
  }

  public static Map<String, CellMap<?>> getCellMap() {
    return Map.ofEntries(
        entry("ref", new CellMap<String>(0, GradeImportDto::getRefFromCell)),
        entry("score", new CellMap<Double>(1, GradeImportDto::getScoreFromCell)));
  }
}
