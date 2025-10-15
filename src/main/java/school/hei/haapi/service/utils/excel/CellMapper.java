package school.hei.haapi.service.utils.excel;

import org.apache.poi.ss.usermodel.Cell;

public record CellMapper(String attributeName, Cell cell, CellMap<?> cellMap) {
}
