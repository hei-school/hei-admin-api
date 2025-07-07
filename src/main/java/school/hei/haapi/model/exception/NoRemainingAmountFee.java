package school.hei.haapi.model.exception;

import school.hei.haapi.model.Fee;

public class NoRemainingAmountFee extends ApiException {
  public NoRemainingAmountFee(Fee fee) {
    super(ExceptionType.SERVER_EXCEPTION, "Remaining amount of %s is already 0".formatted(fee.getId()));
  }
}
