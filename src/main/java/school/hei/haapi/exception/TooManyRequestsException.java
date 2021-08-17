package school.hei.haapi.exception;

public class TooManyRequestsException extends ApiException {
  public TooManyRequestsException(String message) {
    super(message, ExceptionType.CLIENT_EXCEPTION);
  }

  public TooManyRequestsException(Exception source) {
    super(source, ExceptionType.CLIENT_EXCEPTION);
  }
}
