package school.hei.haapi.unit.utils.excel;

import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import school.hei.haapi.service.utils.excel.CellMap;

class ClassWithNoArgsConstructor {
  private final String unmodifiableField;
  private String field;

  public ClassWithNoArgsConstructor() {
    this.unmodifiableField = "dummy";
  }

  private static String getFieldFromCell(Cell cell) {
    return cell.getStringCellValue();
  }

  public static Map<String, CellMap<?>> getValidExcelCellMap() {
    var map = new HashMap<String, CellMap<?>>();
    map.put("field", new CellMap<String>(1, ClassWithNoArgsConstructor::getFieldFromCell));
    return map;
  }

  public static Map<String, CellMap<?>> getInvalidExcelCellMap() {
    var map = new HashMap<String, CellMap<?>>();
    map.put("field", new CellMap<String>(1, ClassWithNoArgsConstructor::getFieldFromCell));
    map.put("notField", new CellMap<String>(1, ClassWithNoArgsConstructor::getFieldFromCell));
    return map;
  }
}
