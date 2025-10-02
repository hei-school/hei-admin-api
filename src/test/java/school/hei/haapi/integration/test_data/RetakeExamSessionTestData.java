package school.hei.haapi.integration.test_data;

import java.time.Instant;
import school.hei.haapi.model.RetakeExamSession;

public class RetakeExamSessionTestData {
  public static RetakeExamSession session1() {
    return RetakeExamSession.builder()
        .title("session1")
        .dateFrom(Instant.parse("2025-09-10T08:00:00.00Z"))
        .dateTo(Instant.parse("2025-09-12T08:00:00.00Z"))
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
