package school.hei.haapi.service.utils;

import static java.time.Month.NOVEMBER;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.regex.Pattern;
import school.hei.haapi.model.exception.BadRequestException;

public record AcademicYear(int startYear) {
  private static final Pattern FORMAT = Pattern.compile("^\\s*(\\d{4})\\s*-\\s*(\\d{4})\\s*$");
  private static final ZoneId SCHOOL_ZONE = ZoneId.of("Indian/Antananarivo");

  public static AcademicYear parse(String value) {
    var matcher = FORMAT.matcher(value == null ? "" : value);
    if (!matcher.matches()) {
      throw invalid(value);
    }
    var startYear = Integer.parseInt(matcher.group(1));
    var endYear = Integer.parseInt(matcher.group(2));
    if (endYear != startYear + 1) {
      throw invalid(value);
    }
    return new AcademicYear(startYear);
  }

  private static BadRequestException invalid(String value) {
    return new BadRequestException(
        "Academic year must be like \"2026 - 2027\", got \"" + value + "\"");
  }

  public String label() {
    return startYear + " - " + (startYear + 1);
  }

  public Instant levelInstant() {
    return LocalDate.of(startYear, NOVEMBER, 15).atStartOfDay(SCHOOL_ZONE).toInstant();
  }

  public Instant badgeExpiration() {
    return LocalDate.of(startYear + 1, NOVEMBER, 1).atStartOfDay(SCHOOL_ZONE).toInstant();
  }
}
