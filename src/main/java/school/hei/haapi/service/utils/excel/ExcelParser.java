package school.hei.haapi.service.utils.excel;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import school.hei.haapi.service.utils.excel.exceptions.ClassInstantiationException;
import school.hei.haapi.service.utils.excel.exceptions.FieldNotFoundException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExcelParser<T> {
    private Map<String, CellMap<?>> columnMap;
    private Class<T> clazz;

    public ExcelParser(Class<T> clazz, Map<String, CellMap<?>> columnMap) {
        this.clazz = clazz;
        var providedFieldNames = columnMap.keySet();
        var classFieldNames = getAllFields().stream().map(Field::getName).toList();
        providedFieldNames.forEach(name -> {
            if (!classFieldNames.contains(name)) {
                throw new FieldNotFoundException(
                        "Provided field " + name + " is not found in class " + clazz.getName()
                );
            }
        });
        this.columnMap = columnMap;
    }

    public List<T> parseFile(File file, int sheetNumber) throws IOException {
        var workbook = generateWorkBook(file);
        var sheet = workbook.getSheetAt(sheetNumber);
        var cellMapEntries = columnMap.entrySet();
        var result = new ArrayList<T>();
        for (var row : sheet) {
            var classInstance = instantiateClass();
            List<CellMapper> cellMappers = cellMapEntries.stream().map(entry ->
                            new CellMapper(entry.getKey(), row.getCell(entry.getValue().colNumber()), entry.getValue()))
                    .toList();
            try {
                cellMappers.forEach(cellMapper -> {
                    try {
                        clazz.getField(cellMapper.attributeName())
                                .set(
                                        classInstance,
                                        cellMapper
                                                .cellMap()
                                                .mapper()
                                                .apply(cellMapper.cell()));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchFieldException e) {
                        throw new FieldNotFoundException(
                                "Provided field " + cellMapper.attributeName() + " is not found in class " + clazz.getName()
                        );
                    }
                });
            } catch (Exception e) {

            }
            result.add(classInstance);
        }
        return result;
    }

    private T instantiateClass() {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new ClassInstantiationException("Cannot instantiate class " + clazz.getName()
                    + ". Target class must have a no args constructor.");
        } catch (Exception e) {
            throw new ClassInstantiationException("Cannot instantiate class " + clazz.getName() + ". " + e);
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
