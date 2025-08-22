package school.hei.haapi.model.exception;

public class ExamsCoefficientSumZero extends RuntimeException {
  public ExamsCoefficientSumZero() {
    super("Sum of coefficient from exams is 0");
  }
}
