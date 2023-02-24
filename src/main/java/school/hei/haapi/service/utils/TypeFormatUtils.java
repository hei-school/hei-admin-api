package school.hei.haapi.service.utils;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import pl.allegro.finance.tradukisto.ValueConverters;

public class TypeFormatUtils {
  private TypeFormatUtils() {

  }

  public static String formatInstant(Instant instant) {
    DateTimeFormatter formatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.FRENCH)
        // Madagascar's timezone
        .withZone(ZoneId.of("UTC+3"));
    return formatter.format(instant);
  }

  public static String formatNumber(int number) {
    NumberFormat numberFormat = NumberFormat.getNumberInstance();
    return numberFormat.format(number);
  }

  public static String formatNumberToWords(int number) {
    ValueConverters intConverter = ValueConverters.FRENCH_INTEGER;
    return intConverter.asWords(number).toUpperCase();
  }

}
