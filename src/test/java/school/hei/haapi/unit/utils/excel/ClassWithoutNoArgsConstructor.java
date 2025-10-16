package school.hei.haapi.unit.utils.excel;

import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import school.hei.haapi.service.utils.excel.CellMap;

public class ClassWithoutNoArgsConstructor {
  private String field;

  public ClassWithoutNoArgsConstructor(String field) {
    this.field = field;
  }

  private static String getFieldFromCell(Cell cell) {
    return cell.getStringCellValue();
  }

  public static Map<String, CellMap<?>> getExcelCellMap() {
    var map = new HashMap<String, CellMap<?>>();
    map.put("field", new CellMap<String>(1, ClassWithoutNoArgsConstructor::getFieldFromCell));
    return map;
  }
}
