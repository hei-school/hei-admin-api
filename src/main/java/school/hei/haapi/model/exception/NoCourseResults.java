package school.hei.haapi.model.exception;

public class NoCourseResults extends RuntimeException {
  public NoCourseResults() {
    super("This student has no course result");
  }
}
