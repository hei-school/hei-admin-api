package school.hei.haapi.model.exception;

import lombok.Getter;

public class ApiException extends RuntimeException {

  @Getter private final ExceptionType type;

  public ApiException(String message, ExceptionType type) {
    super(message);
    this.type = type;
  }

  public ApiException(Exception source, ExceptionType type) {
    super(source);
    this.type = type;
  }

  public enum ExceptionType {
    CLIENT_EXCEPTION,
    SERVER_EXCEPTION
  }
}
