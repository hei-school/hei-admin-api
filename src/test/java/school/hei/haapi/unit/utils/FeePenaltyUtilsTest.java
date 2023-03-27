package school.hei.haapi.unit.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.service.utils.DataFormatterUtils;
import school.hei.haapi.service.utils.FeePenaltyUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeePenaltyUtilsTest {

  private static final int TOTAL_AMOUNT_1 = 1500;
  private static final int REMAIN_AMOUNT_1 = 1000;
  private static final int GRACE_DELAY_1 = 5;
  private static final int INTEREST_PERCENT_1 = 2;
  private static final int APPLICABILITY_DELAY_AFTER_GRACE_1 = 2;
  private static final double EXPECTED_INTEREST = 40.4;

  static Fee fee_1 = Fee.builder()
            .interest(0)
            .status(school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE)
            .creationDatetime(Instant.now())
            .dueDatetime(Instant.now().minus(Duration.ofDays(10)))
            .totalAmount(TOTAL_AMOUNT_1)
            .remainingAmount(REMAIN_AMOUNT_1)
            .build();
  static DelayPenalty delayPenalty_1 = DelayPenalty.builder()
          .graceDelay(GRACE_DELAY_1)
          .interestPercent(INTEREST_PERCENT_1)
          .applicabilityDelayAfterGrace(APPLICABILITY_DELAY_AFTER_GRACE_1)
          .build();

  @Test
  void penalize_fee() {
    Fee actual = FeePenaltyUtils.penalizeFee(fee_1, delayPenalty_1);
    Assertions.assertEquals(EXPECTED_INTEREST, actual.getInterest());
  }

}
