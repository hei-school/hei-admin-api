package school.hei.haapi.exception;

public class NotImplementedException extends ApiException {
  public NotImplementedException(String message) {
    super(message, ExceptionType.SERVER_EXCEPTION);
  }
}
