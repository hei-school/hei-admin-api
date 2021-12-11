package school.hei.haapi.model.exception;

public class NotFoundException extends ApiException {
  public NotFoundException(String message) {
    super(message, ExceptionType.CLIENT_EXCEPTION);
  }
}
