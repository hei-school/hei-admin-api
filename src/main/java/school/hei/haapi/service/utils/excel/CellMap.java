package school.hei.haapi.service.utils.excel;

import org.apache.poi.ss.usermodel.Cell;

import java.util.function.Function;

public record CellMap<T>(int colNumber, Function<Cell, T> mapper) {
}
