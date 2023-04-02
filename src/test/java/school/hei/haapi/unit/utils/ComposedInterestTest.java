package school.hei.haapi.unit.utils;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import school.hei.haapi.service.utils.ComposedInterestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.endpoint.rest.model.InterestTimerate.DAILY;

@Slf4j
public class ComposedInterestTest {
  private static final int INTEREST_PERCENT = 2;
  private static final int AMOUNT = 200_000;
  private static final int expectedDayOneAmount = 204_000;
  private static final int expectedDayTwoAmount = 208_080;
  private static final int expectedDayThreeAmount = 212_241;
  private static final int expectedDayFourAmount = 216_486;
  private static final int expectedDayFiveAmount = 220_816;

  @Test
  void apply_interest_ok() {
    int dayOneAmount =
        ComposedInterestUtils.applyComposedInterestToAmount(INTEREST_PERCENT, 1, DAILY, AMOUNT);
    int dayTwoAmount =
        ComposedInterestUtils.applyComposedInterestToAmount(INTEREST_PERCENT, 2, DAILY, AMOUNT);
    int dayThreeAmount =
        ComposedInterestUtils.applyComposedInterestToAmount(INTEREST_PERCENT, 3, DAILY, AMOUNT);
    int dayFourAmount =
        ComposedInterestUtils.applyComposedInterestToAmount(INTEREST_PERCENT, 4, DAILY, AMOUNT);
    int dayFiveAmount =
        ComposedInterestUtils.applyComposedInterestToAmount(INTEREST_PERCENT, 5, DAILY, AMOUNT);

    log.info("amounts = {}", List.of(dayOneAmount, dayTwoAmount, dayThreeAmount, dayFourAmount,
        dayFiveAmount));

    assertEquals(expectedDayOneAmount, dayOneAmount);
    assertEquals(expectedDayTwoAmount, dayTwoAmount);
    assertEquals(expectedDayThreeAmount, dayThreeAmount);
    assertEquals(expectedDayFourAmount, dayFourAmount);
    assertEquals(expectedDayFiveAmount, dayFiveAmount);
  }
}
