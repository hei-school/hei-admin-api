package school.hei.haapi.model.exception;

public class ForbiddenException extends ApiException {

  public ForbiddenException() {
    super(ExceptionType.CLIENT_EXCEPTION, "Access is denied");
  }

  public ForbiddenException(String message) {
    super(ExceptionType.CLIENT_EXCEPTION, message);
  }
}
