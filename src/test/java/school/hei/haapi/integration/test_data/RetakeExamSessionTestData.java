package school.hei.haapi.integration.test_data;

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

  public static RetakeExamSession session2() {
    return RetakeExamSession.builder()
        .id("session2_id")
        .title("session2")
        .dateFrom(LocalDateTime.now().plusMonths(2).toInstant(ZoneOffset.UTC))
        .dateTo(LocalDateTime.now().plusMonths(2).plusDays(20).toInstant(ZoneOffset.UTC))
        .build();
  }

  public static RetakeExamSession session3() {
    return RetakeExamSession.builder()
        .id("session3_id")
        .title("session3")
        .dateFrom(LocalDateTime.now().plusMonths(3).toInstant(ZoneOffset.UTC))
        .dateTo(LocalDateTime.now().plusMonths(3).plusDays(20).toInstant(ZoneOffset.UTC))
        .build();
  }

  public static RetakeExamSession sessionWithWrongDate() {
    return RetakeExamSession.builder()
        .title("session with wrong date")
        .dateFrom(LocalDateTime.now().plusDays(10).toInstant(ZoneOffset.UTC))
        .dateTo(LocalDateTime.now().minusDays(10).toInstant(ZoneOffset.UTC))
        .build();
  }
}
