package school.hei.haapi.model.exception;

public class NotImplementedException extends ApiException {
  public NotImplementedException(String message) {
    super(message, ExceptionType.SERVER_EXCEPTION);
  }
}
