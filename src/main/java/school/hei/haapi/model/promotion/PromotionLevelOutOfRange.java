package school.hei.haapi.model.promotion;

public class PromotionLevelOutOfRange extends RuntimeException {
  public PromotionLevelOutOfRange(int yearOfStudying) {
    super("Promotion level out of range, already %d years old".formatted(yearOfStudying));
  }
}
