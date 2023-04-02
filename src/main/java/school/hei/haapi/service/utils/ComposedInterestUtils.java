package school.hei.haapi.service.utils;

import school.hei.haapi.endpoint.rest.model.InterestTimerate;
import school.hei.haapi.model.exception.NotImplementedException;

import static school.hei.haapi.endpoint.rest.model.InterestTimerate.DAILY;

public class ComposedInterestUtils {
  private ComposedInterestUtils() {
  }

  public static int applyComposedInterestToAmount(
      int interest, long timeLapse,
      InterestTimerate interestTimerate,
      int amount) {
    if (interestTimerate != DAILY) {
      throw
          new NotImplementedException("Applying monthly composed interest is not yet supported. ");
    }
    if (amount == 0) {
      return 0;
    }
    double result = amount;
    for (int i = 1; i <= timeLapse; i++) {
      double addedAmount = result * ((double) interest / 100.0);
      result = addedAmount + result;
    }
    return ((int) result);
  }
}
