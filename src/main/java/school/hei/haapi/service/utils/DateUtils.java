package school.hei.haapi.service.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class DateUtils {
  public static String getRecoveryDate(String dueDateString) {

    dueDateString = normalizeDateString(dueDateString);

    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
    LocalDate dueDate = LocalDate.parse(dueDateString, inputFormatter);

    LocalDate recoveryDate = dueDate.plusDays(15);

    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
    return recoveryDate.format(outputFormatter);
  }

  private static String normalizeDateString(String dateString) {
    return dateString.replaceAll("(?<=\\b)([1-9])(?=\\s[a-zA-Z])", "0$1");
  }
}
