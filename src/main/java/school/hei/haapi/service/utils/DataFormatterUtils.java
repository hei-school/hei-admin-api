package school.hei.haapi.service.utils;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import pl.allegro.finance.tradukisto.ValueConverters;

public class DataFormatterUtils {
  private DataFormatterUtils() {}

  public static String formatLocalDate(LocalDate localDate, String format) {
    DateTimeFormatter pattern = DateTimeFormatter.ofPattern(format);
    return localDate.format(pattern);
  }

  public static String localDateToCommonDate(LocalDate localDate) {
    DateTimeFormatter formatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(Locale.FRENCH)
            .withZone(ZoneId.of("UTC+3"));
    return formatter.format(localDate);
  }

  public static String instantToCommonDate(Instant instant) {
    DateTimeFormatter formatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(Locale.FRENCH)
            // Madagascar's timezone
            .withZone(ZoneId.of("UTC+3"));
    return formatter.format(instant);
  }

  public static String numberToReadable(int number) {
    NumberFormat numberFormat = NumberFormat.getNumberInstance();
    return numberFormat.format(number);
  }

  public static String numberToWords(int number) {
    ValueConverters intConverter = ValueConverters.FRENCH_INTEGER;
    return intConverter.asWords(number).toUpperCase();
  }

  public static boolean isLate(Instant instantToCompare) {

    Instant now = Instant.now();

    return now.isAfter(instantToCompare);
  }

  public static <T extends Enum<T>> T fromValue(Class<T> enumClass, String value) {
    for (T enumConstant : enumClass.getEnumConstants()) {
      if (enumConstant.name().equals(value)) {
        return enumConstant;
      }
    }
    throw new IllegalArgumentException(
        "Unexpected value '" + value + "' for enum " + enumClass.getSimpleName());
  }
}
