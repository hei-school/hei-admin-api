package school.hei.haapi.service.utils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.FrequencyScopeDay;

@Component
public class DateProducerFrom {

  public List<LocalDate> apply(FrequencyScopeDay dayOfWeek, int goalDays, Instant from) {
    List<LocalDate> days = new ArrayList<>();
    DayOfWeek targetDay = DayOfWeek.valueOf(dayOfWeek.getValue());
    TemporalField fieldISO = WeekFields.of(Locale.FRANCE).dayOfWeek();
    LocalDate date = LocalDate.ofInstant(from, ZoneId.of("UTC+3")).plusWeeks(1).with(fieldISO, 1);

    while (date.getDayOfWeek() != targetDay) {
      date = date.plusDays(1);
    }

    for (int i = 0; i < goalDays; i++) {
      days.add(date);
      date = date.plusWeeks(1);
    }

    return days;
  }
}
