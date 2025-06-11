package school.hei.haapi.unit.fee;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeStatusHistory;

class FeeStatusAtTest {
  @Test
  void get_status_at_instant_ok() {
    assertEquals(PENDING, fee().getStatusAt(Instant.parse("2022-01-01T00:00:00.00Z")).get());
    assertEquals(PAID, fee().getStatusAt(Instant.parse("2022-01-02T00:00:00.00Z")).get());
    assertEquals(UNPAID, fee().getStatusAt(Instant.parse("2021-12-16T00:00:00.00Z")).get());
    assertEquals(empty(), fee().getStatusAt(Instant.parse("2020-12-16T00:00:00.00Z")));
  }

  private Fee fee() {
    return Fee.builder()
        .statusHistories(
            List.of(
                FeeStatusHistory.builder()
                    .status(PAID)
                    .datetime(Instant.parse("2022-01-02T00:00:00.00Z"))
                    .build(),
                FeeStatusHistory.builder()
                    .status(PENDING)
                    .datetime(Instant.parse("2021-12-31T00:00:00.00Z"))
                    .build(),
                FeeStatusHistory.builder()
                    .status(UNPAID)
                    .datetime(Instant.parse("2021-12-01T00:00:00.00Z"))
                    .build()))
        .build();
  }
}
