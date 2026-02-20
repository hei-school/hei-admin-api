package school.hei.haapi.model.promotion;

public class PromotionLevelOutOfRange extends RuntimeException {
  public PromotionLevelOutOfRange() {
    super("Promotion level out of range, cycle may not be linked to any levels");
  }

  public PromotionLevelOutOfRange(int yearOfStudying) {
    super("Promotion level out of range, already %d years old".formatted(yearOfStudying));
  }
}
