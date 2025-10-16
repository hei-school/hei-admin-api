package school.hei.haapi.service.utils.excel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import school.hei.haapi.service.utils.excel.exceptions.ClassInstantiationException;
import school.hei.haapi.service.utils.excel.exceptions.FieldAccessException;
import school.hei.haapi.service.utils.excel.exceptions.FieldNotFoundException;

public class ExcelParser<T> {
  private final Map<String, CellMap<?>> columnMap;
  private final Class<T> clazz;

  public ExcelParser(Class<T> clazz, Map<String, CellMap<?>> columnMap) {
    this.clazz = clazz;
    var providedFieldNames = columnMap.keySet();
    var classFieldNames = getAllFields().stream().map(Field::getName).toList();
    providedFieldNames.forEach(
        name -> {
          if (!classFieldNames.contains(name)) {
            throw new FieldNotFoundException(
                "Provided field %s is not found in class %s".formatted(name, clazz.getName()));
          }
        });
    this.columnMap = columnMap;
  }

  public List<T> parseFile(File file, int sheetNumber, Row.MissingCellPolicy missingCellPolicy)
      throws IOException {
    var workbook = generateWorkBook(file);
    var sheet = workbook.getSheetAt(sheetNumber);
    var cellMapEntries = columnMap.entrySet();
    var result = new ArrayList<T>();
    for (var row : sheet) {
      var classInstance = instantiateClass();
      List<CellMapper> cellMappers =
          cellMapEntries.stream()
              .map(
                  entry ->
                      new CellMapper(
                          entry.getKey(),
                          row.getCell(entry.getValue().colNumber(), missingCellPolicy),
                          entry.getValue()))
              .toList();
      try {
        cellMappers.forEach(cellMapper -> setFieldValue(classInstance, cellMapper));
      } catch (IllegalArgumentException | IllegalStateException e) {
        continue;
      }
      result.add(classInstance);
    }
    return result;
  }

  private void setFieldValue(T classInstance, CellMapper cellMapper) {
    try {
      var toSetField = clazz.getDeclaredField(cellMapper.attributeName());
      toSetField.setAccessible(true);
      toSetField.set(classInstance, cellMapper.cellMap().mapper().apply(cellMapper.cell()));
      toSetField.setAccessible(false);
    } catch (IllegalAccessException e) {
      throw new FieldAccessException(
          "Cannot access field in class %s. %s".formatted(clazz.getName(), e));
    } catch (NoSuchFieldException e) {
      throw new FieldNotFoundException(
          "Provided field is not found in class %s. %s".formatted(clazz.getName(), e));
    }
  }

  private T instantiateClass() {
    try {
      return clazz.getConstructor().newInstance();
    } catch (NoSuchMethodException e) {
      throw new ClassInstantiationException(
          "Cannot instantiate class %s. Target class must have a no args constructor."
              .formatted(clazz.getName()));
    } catch (Exception e) {
      throw new ClassInstantiationException(
          "Cannot instantiate class %s. %s".formatted(clazz.getName(), e));
    }
  }

  private List<Field> getAllFields() {
    List<Field> fields = new ArrayList<>();
    Class<?> currentClass = clazz;

    while (currentClass != null && currentClass != Object.class) {
      fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
      currentClass = currentClass.getSuperclass();
    }
    return fields;
  }

  private Workbook generateWorkBook(File file) throws IOException {
    try {
      return WorkbookFactory.create(file);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }
}
