package school.hei.haapi.model.promotion;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import school.hei.haapi.model.exception.ApiException;

public class PromotionLevelOutOfRangeException extends ApiException {
  public PromotionLevelOutOfRangeException() {
    super(SERVER_EXCEPTION, "Promotion level out of range, cycle may not be linked to any levels");
  }

  public PromotionLevelOutOfRangeException(int yearOfStudying) {
    super(
        SERVER_EXCEPTION,
        "Promotion level out of range, already %d years old".formatted(yearOfStudying));
  }
}
