package school.hei.haapi.service.utils;

import java.time.Instant;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

public class DateTimeUtils {
    public DateTimeUtils() {
    }

    public static GregorianCalendar instantToGregorianCalendar(Instant instant) {
        String[] ts = instant.toString().split("T");
        List<String> strings = Arrays.stream(ts).collect(Collectors.toList());
        strings.remove(1);

        List<Integer> yearMonthDay = Arrays.stream(strings.get(0).split("-"))
                .collect(Collectors.toList())
                .stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        int day = yearMonthDay.get(2);
        int month = yearMonthDay.get(1);
        int year = yearMonthDay.get(0);
        return new GregorianCalendar(year, month, day);
    }
}
