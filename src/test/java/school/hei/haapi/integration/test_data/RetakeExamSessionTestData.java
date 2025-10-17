package school.hei.haapi.integration.test_data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import school.hei.haapi.model.RetakeExamSession;

public class RetakeExamSessionTestData {
  public static RetakeExamSession session1() {
    return RetakeExamSession.builder()
        .id("session1_id")
        .title("session1")
        .dateFrom(LocalDateTime.now().plusMonths(1).toInstant(ZoneOffset.UTC))
        .dateTo(LocalDateTime.now().plusMonths(1).plusDays(20).toInstant(ZoneOffset.UTC))
        .build();
  }

  public static RetakeExamSession sessionWithWrongDate() {
    return RetakeExamSession.builder()
        .title("session with wrong date")
        .dateFrom(Instant.parse("2025-09-21T00:00:00.00Z"))
        .dateTo(Instant.parse("2025-09-11T00:00:00.00Z"))
        .build();
  }
}
