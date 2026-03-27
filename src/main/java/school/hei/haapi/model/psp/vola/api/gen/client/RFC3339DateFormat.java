package school.hei.haapi.model.psp.vola.api.gen.client;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class RFC3339DateFormat extends DateFormat {

  private static final StdDateFormat STD_DATE_FORMAT = new StdDateFormat();
  private static final SimpleDateFormat ISO_FORMAT;

  static {
    ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    ISO_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Override
  public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
    String value = ISO_FORMAT.format(date);
    toAppendTo.append(value);
    return toAppendTo;
  }

  @Override
  public Date parse(String source, java.text.ParsePosition pos) {
    return STD_DATE_FORMAT.parse(source, pos);
  }
}
