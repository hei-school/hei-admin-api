package school.hei.haapi.model.dto;

import java.time.Instant;

public record GroupFlowPeriod(Instant start, Instant end) {
  public boolean contains(Instant instant) {
    boolean afterStart = !instant.isBefore(start);
    boolean beforeEnd = (end == null) || instant.isBefore(end);
    return afterStart && beforeEnd;
  }
}
