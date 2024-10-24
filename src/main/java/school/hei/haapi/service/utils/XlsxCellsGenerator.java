package school.hei.haapi.service.utils;

import static school.hei.haapi.service.utils.DataFormatterUtils.formatLocalDate;
import static school.hei.haapi.service.utils.DataFormatterUtils.instantToOcsDateFormat;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class XlsxCellsGenerator<T> implements Function<List<T>, byte[]> {
  @Override
  @SneakyThrows
  public byte[] apply(List<T> t) {
    Workbook workbook = new XSSFWorkbook();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Sheet sheet = workbook.createSheet("excel-sheet");
    Row row0 = sheet.createRow(0);

    generateHeadedRow(row0, t.get(0));

    for (int i = 0; i < t.size(); i++) {
      Row rowi = sheet.createRow(i + 1);
      generateBodiesRow(rowi, t.get(i));
    }

    try {
      workbook.write(bos);
    } finally {
      bos.close();
    }
    return bos.toByteArray();
  }

  @SneakyThrows
  private void generateBodiesRow(Row rowi, T t) {
    var objectClazz = t.getClass();
    var fields = Arrays.stream(objectClazz.getDeclaredFields()).map(Field::getName).toArray();

    for (int i = 0; i < fields.length; i++) {
      var fieldValue = objectClazz.getDeclaredField(fields[i].toString());
      fieldValue.setAccessible(true);
      Object value = fieldValue.get(t);

      String formatedValue = formatValue(value);
      rowi.createCell(i).setCellValue(formatedValue != null ? formatedValue : "");
    }
  }

  private void generateHeadedRow(Row row0, T objectToPrint) {
    var objectClazz = objectToPrint.getClass();
    var fields = Arrays.stream(objectClazz.getDeclaredFields()).map(Field::getName).toArray();

    for (int i = 0; i < fields.length; i++) {
      row0.createCell(i).setCellValue(fields[i].toString());
    }
  }

  private String formatValue(Object value) {
    if (value instanceof LocalDate) {
      return formatLocalDate((LocalDate) value);
    } else if (value instanceof Instant) {
      return instantToOcsDateFormat((Instant) value);
    } else {
      return value != null ? value.toString() : null;
    }
  }
}
