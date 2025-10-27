package school.hei.haapi.service.utils.excel;

import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;

public record ParseResult<T>(List<T> parsedResult, Map<Row, Exception> skippedRows) {}
