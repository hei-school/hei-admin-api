package school.hei.haapi.service.utils;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.GregorianCalendar;

public class DateTimeUtils {
    public DateTimeUtils() {
    }

    public static GregorianCalendar instantToGregorianCalendar(Instant instant) {
        int day = instant.get(ChronoField.DAY_OF_MONTH);
        int month = instant.get(ChronoField.MONTH_OF_YEAR);
        int year = instant.get(ChronoField.YEAR);
        return new GregorianCalendar(year, month, day);
    }
}
