package school.hei.haapi.model.promotion;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import school.hei.haapi.model.exception.ApiException;

public class PromotionLevelOutOfRangeException extends ApiException {
  public PromotionLevelOutOfRangeException() {
    super(SERVER_EXCEPTION, "Promotion level out of range, cycle may not be linked to any levels");
  }

  public PromotionLevelOutOfRangeException(int yearOfStudying) {
    super(SERVER_EXCEPTION, messageFor(yearOfStudying));
  }

  // A negative year is not an overshoot but the opposite: students are already attached to a
  // promotion whose scholar year has not begun. Saying "already -1 years old" reads as corrupt
  // data when nothing is wrong.
  private static String messageFor(int yearOfStudying) {
    return yearOfStudying < 0
        ? "Promotion level out of range, promotion starts in %d scholar year(s)"
            .formatted(-yearOfStudying)
        : "Promotion level out of range, already %d years old".formatted(yearOfStudying);
  }
}
