package school.hei.haapi.service.utils.excel;

import java.util.function.Function;
import org.apache.poi.ss.usermodel.Cell;

public record CellMap<T>(int colNumber, Function<Cell, T> mapper) {}
