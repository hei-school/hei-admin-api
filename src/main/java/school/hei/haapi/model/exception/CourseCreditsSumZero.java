package school.hei.haapi.model.exception;

public class CourseCreditsSumZero extends RuntimeException {
  public CourseCreditsSumZero() {
    super("Sum of credits between exams is 0");
  }
}
