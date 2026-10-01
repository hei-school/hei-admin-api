package school.hei.haapi.service.utils;

import static java.time.Month.NOVEMBER;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import school.hei.haapi.model.exception.BadRequestException;

public record AcademicYear(int startYear) {
  private static final Pattern FORMAT = Pattern.compile("^\\s*(\\d{4})\\s*-\\s*(\\d{4})\\s*$");
  private static final ZoneId SCHOOL_ZONE = ZoneId.of("Indian/Antananarivo");

  public static AcademicYear parse(String value) {
    Matcher matcher = FORMAT.matcher(value == null ? "" : value);
    if (!matcher.matches()
        || Integer.parseInt(matcher.group(2)) != Integer.parseInt(matcher.group(1)) + 1) {
      throw new BadRequestException(
          "Academic year must be like \"2026 - 2027\", got \"" + value + "\"");
    }
    return new AcademicYear(Integer.parseInt(matcher.group(1)));
  }

  public String label() {
    return startYear + " - " + (startYear + 1);
  }

  public Instant levelInstant() {
    return LocalDate.of(startYear, NOVEMBER, 15).atStartOfDay(SCHOOL_ZONE).toInstant();
  }

  /** The school year starts in November: badges stop working when the next one starts. */
  public Instant badgeExpiration() {
    return LocalDate.of(startYear + 1, NOVEMBER, 1).atStartOfDay(SCHOOL_ZONE).toInstant();
  }
}
