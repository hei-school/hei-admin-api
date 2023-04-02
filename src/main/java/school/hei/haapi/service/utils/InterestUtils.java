package school.hei.haapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.NotYetImplementedException;
import school.hei.haapi.model.DelayPenalty.InterestTimerate;

import static school.hei.haapi.model.DelayPenalty.InterestTimerate.DAILY;

@Slf4j
public class InterestUtils {
  private InterestUtils() {
  }

  /**
   * return the amount after applying it to compound interest.
   * <p>
   * only <b>DAILY</b> time rate is available for now
   * </p>
   */
  public static int applyCompoundInterest(
      int amount, int interestPercent,
      long period, InterestTimerate interestTimerate) {
    if (interestTimerate != DAILY) {
      throw new NotYetImplementedException("Only 'DAILY' time rate is available for now");
    }

    if (amount == 0) {
      return 0;
    }

    double applied = amount;
    for (int i = 1; i <= period; i++) {
      applied += doApplyInterest(applied, interestPercent);
    }

    return ((int) applied);
  }

  private static double doApplyInterest(double amount, int interestPercent) {
    return amount * ((double) interestPercent / 100.0);
  }
}
