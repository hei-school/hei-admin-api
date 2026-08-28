package school.hei.haapi.unit.utils.excel;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import school.hei.haapi.integration.conf.TestFiles;
import school.hei.haapi.service.utils.excel.ExcelParser;
import school.hei.haapi.service.utils.excel.exceptions.ClassInstantiationException;
import school.hei.haapi.service.utils.excel.exceptions.FieldNotFoundException;

public class ExcelParserTest {
  @TempDir private Path tempDir;

  @Test()
  void instantiate_class_without_no_args_constructor_ko() throws IOException {
    var dummyFile = TestFiles.getMockedFile("dummy-excel", ".xlsx");
    var subject =
        new ExcelParser<>(
            ClassWithoutNoArgsConstructor.class, ClassWithoutNoArgsConstructor.getExcelCellMap());
    assertThrows(
        ClassInstantiationException.class,
        () -> subject.parseFile(dummyFile, 0, CREATE_NULL_AS_BLANK));
  }

  @Test
  void map_with_innexistant_field_ko() {
    assertThrows(
        FieldNotFoundException.class,
        () ->
            new ExcelParser<>(
                ClassWithNoArgsConstructor.class,
                ClassWithNoArgsConstructor.getInvalidExcelCellMap()));
  }
}
