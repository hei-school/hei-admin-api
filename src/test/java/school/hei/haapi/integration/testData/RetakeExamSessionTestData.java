package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M2;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import school.hei.haapi.model.RetakeExamSession;

public class RetakeExamSessionTestData {

  public static RetakeExamSession session1() {
    return RetakeExamSession.builder()
        .id(randomUUID().toString())
        .title("session1-" + randomUUID())
        .studentLevels(List.of(M2))
        .dateFrom(LocalDateTime.now().plusMonths(1).toInstant(ZoneOffset.UTC))
        .dateTo(LocalDateTime.now().plusMonths(1).plusDays(20).toInstant(ZoneOffset.UTC))
        .build();
  }

  public static RetakeExamSession session2() {
    return RetakeExamSession.builder()
        .id(randomUUID().toString())
        .title("session2-" + randomUUID())
        .studentLevels(List.of(M1, M2))
        .dateFrom(LocalDateTime.now().plusMonths(2).toInstant(ZoneOffset.UTC))
        .dateTo(LocalDateTime.now().plusMonths(2).plusDays(20).toInstant(ZoneOffset.UTC))
        .build();
  }

  public static RetakeExamSession session3() {
    return RetakeExamSession.builder()
        .id(randomUUID().toString())
        .title("session3-" + randomUUID())
        .studentLevels(List.of(L2, L3))
        .dateFrom(LocalDateTime.now().plusMonths(3).toInstant(ZoneOffset.UTC))
        .dateTo(LocalDateTime.now().plusMonths(3).plusDays(20).toInstant(ZoneOffset.UTC))
        .build();
  }

  public static RetakeExamSession passedSession() {
    return RetakeExamSession.builder()
        .id(randomUUID().toString())
        .title("session4-" + randomUUID())
        .studentLevels(List.of(L1, L2))
        .dateFrom(LocalDateTime.now().minusMonths(3).toInstant(ZoneOffset.UTC))
        .dateTo(LocalDateTime.now().minusMonths(3).plusDays(20).toInstant(ZoneOffset.UTC))
        .build();
  }

  public static RetakeExamSession sessionWithWrongDate() {
    return RetakeExamSession.builder()
        .title("session with wrong date")
        .studentLevels(List.of(L1))
        .dateFrom(LocalDateTime.now().plusDays(10).toInstant(ZoneOffset.UTC))
        .dateTo(LocalDateTime.now().minusDays(10).toInstant(ZoneOffset.UTC))
        .build();
  }
}
