package school.hei.haapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.util.StringUtil;
import school.hei.haapi.service.utils.excel.CellMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Map.entry;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class GradeImportDto {
    private String ref;
    private Double score;

    private static final String REF_REGEX = "^STD[\\w-]+$";

    private static String getRefFromCell(Cell cell){
        verifyEmptyCell(cell);
        return validateRefCell(cell);
    }

    private static Double getScoreFromCell(Cell cell){
        try{
            verifyEmptyCell(cell);
            var value = cell.getStringCellValue();
            if(value != null){
                return Double.parseDouble(value);
            }
            return null;
        }catch (NumberFormatException e){
            throw new IllegalStateException(
                    "ligne %d ignorée en raison d'une valeur incovertible en décimal"
                            .formatted(cell.getRowIndex()));
        }
    }
    private static void verifyEmptyCell(Cell cell) {
        if (StringUtil.isBlank(cell.getStringCellValue())) {
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

    public static Map<String, CellMap<?>> getCellMap(){
       return Map.ofEntries(
               entry("ref", new CellMap<String>(0, GradeImportDto::getRefFromCell)),
               entry("score", new CellMap<Double>(1, GradeImportDto::getScoreFromCell))
       );
    }
}
