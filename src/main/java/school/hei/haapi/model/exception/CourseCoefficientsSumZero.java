package school.hei.haapi.model.exception;

public class CourseCoefficientsSumZero extends RuntimeException {
  public CourseCoefficientsSumZero() {
    super("Sum of coefficient between exams is 0");
  }
}
