package school.hei.haapi.unit.utils;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.JUNE;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.service.utils.SchoolYearSupplier;

@ExtendWith(MockitoExtension.class)
class SchoolYearSupplierTest {

  @InjectMocks private SchoolYearSupplier subject;

  @Test
  void getSchoolYear_shouldReturnCurrentAndNextYear_whenCurrentMonthIsOctober() {
    var octoberDate = LocalDate.of(2023, OCTOBER, 15);
    try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
      mockedLocalDate.when(LocalDate::now).thenReturn(octoberDate);

      var result = subject.get();

      assertEquals("2023 - 2024", result);
    }
  }

  @Test
  void getSchoolYear_shouldReturnCurrentAndNextYear_whenCurrentMonthIsNovember() {
    var novemberDate = LocalDate.of(2023, NOVEMBER, 15);
    try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
      mockedLocalDate.when(LocalDate::now).thenReturn(novemberDate);

      var result = subject.get();

      assertEquals("2023 - 2024", result);
    }
  }

  @Test
  void getSchoolYear_shouldReturnCurrentAndNextYear_whenCurrentMonthIsDecember() {
    var decemberDate = LocalDate.of(2023, DECEMBER, 15);
    try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
      mockedLocalDate.when(LocalDate::now).thenReturn(decemberDate);

      var result = subject.get();

      assertEquals("2023 - 2024", result);
    }
  }

  @Test
  void getSchoolYear_shouldReturnPreviousAndCurrentYear_whenCurrentMonthIsJanuary() {
    var januaryDate = LocalDate.of(2024, JANUARY, 15);
    try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
      mockedLocalDate.when(LocalDate::now).thenReturn(januaryDate);

      var result = subject.get();

      assertEquals("2023 - 2024", result);
    }
  }

  @Test
  void getSchoolYear_shouldReturnPreviousAndCurrentYear_whenCurrentMonthIsSeptember() {
    var septemberDate = LocalDate.of(2023, SEPTEMBER, 15);
    try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
      mockedLocalDate.when(LocalDate::now).thenReturn(septemberDate);

      var result = subject.get();

      assertEquals("2022 - 2023", result);
    }
  }

  @Test
  void getSchoolYear_shouldHandleYearBoundaryCorrectly() {
    var dec31 = LocalDate.of(2023, DECEMBER, 31);
    try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
      mockedLocalDate.when(LocalDate::now).thenReturn(dec31);

      var result = subject.get();

      assertEquals("2023 - 2024", result);
    }

    var jan1 = LocalDate.of(2024, JANUARY, 1);
    try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
      mockedLocalDate.when(LocalDate::now).thenReturn(jan1);

      var result = subject.get();

      assertEquals("2023 - 2024", result);
    }
  }

  @Test
  void get_shouldCallGetSchoolYear() {
    var testDate = LocalDate.of(2023, JUNE, 15);
    try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
      mockedLocalDate.when(LocalDate::now).thenReturn(testDate);

      var result = subject.get();

      assertEquals("2022 - 2023", result);
    }
  }
}
