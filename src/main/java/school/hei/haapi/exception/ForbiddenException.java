package school.hei.haapi.exception;

public class ForbiddenException extends ApiException {
  public ForbiddenException(String message) {
    super(message, ExceptionType.CLIENT_EXCEPTION);
  }
}
