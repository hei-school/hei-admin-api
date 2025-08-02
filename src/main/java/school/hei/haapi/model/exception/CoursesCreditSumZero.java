package school.hei.haapi.model.exception;

public class CoursesCreditSumZero extends RuntimeException {
  public CoursesCreditSumZero() {
    super("Sum of credits from courses is 0");
  }
}
