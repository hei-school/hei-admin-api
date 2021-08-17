package school.hei.haapi.exception;

public class BadRequestException extends ApiException {
  public BadRequestException(String message) {
    super(message, ExceptionType.CLIENT_EXCEPTION);
  }
}
