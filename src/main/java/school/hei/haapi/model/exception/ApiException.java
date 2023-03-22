package school.hei.haapi.model.exception;

import lombok.Getter;

public class ApiException extends RuntimeException {

  @Getter
  private final ExceptionType type;

  public ApiException(ExceptionType type, String message) {
    super(message);
    this.type = type;
  }

  public ApiException(ExceptionType type, Exception source) {
    super(source);
    this.type = type;
  }

  public enum ExceptionType {
    CLIENT_EXCEPTION,
    SERVER_EXCEPTION
  }
}
