package school.hei.haapi.endpoint.rest.model.exception;

public class BadRequestException extends ApiException {
  public BadRequestException(String message) {
    super(ExceptionType.CLIENT_EXCEPTION, message);
  }
}
