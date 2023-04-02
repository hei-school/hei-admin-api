package school.hei.haapi.unit.utils;

import org.junit.jupiter.api.Test;
import school.hei.haapi.service.utils.InterestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.model.DelayPenalty.InterestTimerate.DAILY;

class InterestTest {
  private static final int INTEREST_PERCENT = 2;
  private static final int AMOUNT = 200_000;
  private static final int expected_Day1 = 204_000;
  private static final int expected_Day2 = 208_080;
  private static final int expected_Day3 = 212_241;
  private static final int expected_Day4 = 216_486;
  private static final int expected_Day5 = 220_816;

  @Test
  void apply_interest_ok() {
    int day1_amount =
        InterestUtils.applyCompoundInterest(AMOUNT, INTEREST_PERCENT, 1, DAILY);
    int day2_amount =
        InterestUtils.applyCompoundInterest(AMOUNT, INTEREST_PERCENT, 2, DAILY);
    int day3_amount =
        InterestUtils.applyCompoundInterest(AMOUNT, INTEREST_PERCENT, 3, DAILY);
    int day4_amount =
        InterestUtils.applyCompoundInterest(AMOUNT, INTEREST_PERCENT, 4, DAILY);
    int day5_amount =
        InterestUtils.applyCompoundInterest(AMOUNT, INTEREST_PERCENT, 5, DAILY);

    assertEquals(expected_Day1, day1_amount);
    assertEquals(expected_Day2, day2_amount);
    assertEquals(expected_Day3, day3_amount);
    assertEquals(expected_Day4, day4_amount);
    assertEquals(expected_Day5, day5_amount);
  }
}