package school.hei.haapi.service.befiana;

import lombok.Getter;

@Getter
public class BefianaException extends RuntimeException {
  private final Integer httpStatus;

  public BefianaException(String message, Integer httpStatus, Throwable cause) {
    super(message, cause);
    this.httpStatus = httpStatus;
  }
}
