package school.hei.haapi.model.dto;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static school.hei.haapi.service.utils.DateUtils.convertStringToInstant;

import io.micrometer.common.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.service.utils.excel.CellMap;

@NoArgsConstructor
@Getter
@Setter
@Slf4j
public class MobileTransactionDetailsDto {
  private String dateTransactionCreation;
  private String timeTransactionCreation;
  private String pspTransactionRef;
  private MpbsStatus mpbsStatus;
  private int pspTransactionAmount;

  private static String mapCellToDateTransactionCreation(Cell cell) {
    if (StringUtils.isBlank(cell.getStringCellValue())) {
      log.warn("Row {} ignored because of an empty cell", cell.getRowIndex());
      throw new IllegalArgumentException(
          "Cell %d:%d is empty".formatted(cell.getRowIndex(), cell.getColumnIndex()));
    }
    return cell.getStringCellValue().trim();
  }

  private static String mapCellToTimeTransactionCreation(Cell cell) {
    if (StringUtils.isBlank(cell.getStringCellValue())) {
      log.warn("Row {} ignored because of an empty cell", cell.getAddress());
      throw new IllegalArgumentException(
          "Cell %d:%d is empty".formatted(cell.getRowIndex(), cell.getColumnIndex()));
    }
    return cell.getStringCellValue().trim();
  }

  private static String mapCellToPspTransactionRef(Cell cell) {
      if(StringUtils.isBlank(cell.getStringCellValue())) {
          log.warn("Row {} ignored because of an empty cell", cell.getRowIndex());
          throw new IllegalArgumentException(
              "Cell %d:%d is empty".formatted(cell.getRowIndex(), cell.getColumnIndex()));
      }
      var ref = cell.getStringCellValue().trim();
    if (!ref.matches("^MP.{18}$")) {
      throw new IllegalArgumentException(
          "Row %d ignored because of an invalid transaction ref".formatted(cell.getRowIndex()));
    }
    return ref;
  }

  private static MpbsStatus mapCellToMpbsStatus(Cell cell) {
      if(StringUtils.isBlank(cell.getStringCellValue())) {
          log.warn("Row {} ignored because of an empty cell", cell.getRowIndex());
          throw new IllegalArgumentException(
              "Cell %d:%d is empty".formatted(cell.getRowIndex(), cell.getColumnIndex()));
      }
    return MpbsStatus.fromValue(
        Objects.equals(cell.getStringCellValue().trim(), "Succès") ? "SUCCESS" : "FAILED");
  }

  private static int mapCellToPspTransactionAmount(Cell cell) {
    return (int) cell.getNumericCellValue();
  }

  public MobileTransactionDetails toModel() {
    String dateTimeStr = dateTransactionCreation + " " + timeTransactionCreation;
    return MobileTransactionDetails.builder()
        .id(randomUUID().toString())
        .pspDatetimeTransactionCreation(convertStringToInstant(dateTimeStr))
        .pspTransactionRef(pspTransactionRef)
        .pspTransactionAmount(pspTransactionAmount)
        .status(mpbsStatus)
        .pspOwnDatetimeVerification(now())
        .build();
  }

  public static Map<String, CellMap<?>> getExcelColumnMap() {
    var columnMap = new HashMap<String, CellMap<?>>();
    columnMap.put(
        "dateTransactionCreation",
        new CellMap<String>(1, MobileTransactionDetailsDto::mapCellToDateTransactionCreation));
    columnMap.put(
        "timeTransactionCreation",
        new CellMap<String>(2, MobileTransactionDetailsDto::mapCellToTimeTransactionCreation));
    columnMap.put(
        "pspTransactionRef",
        new CellMap<String>(3, MobileTransactionDetailsDto::mapCellToPspTransactionRef));
    columnMap.put(
        "mpbsStatus", new CellMap<MpbsStatus>(6, MobileTransactionDetailsDto::mapCellToMpbsStatus));
    columnMap.put(
        "pspTransactionAmount",
        new CellMap<Integer>(14, MobileTransactionDetailsDto::mapCellToPspTransactionAmount));
    return columnMap;
  }
}
