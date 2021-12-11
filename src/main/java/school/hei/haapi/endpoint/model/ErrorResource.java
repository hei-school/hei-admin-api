package school.hei.haapi.endpoint.model;

import lombok.Value;

@Value
public class ErrorResource {
  Type errorType;
  String message;

  public enum Type {
    // client errors
    BAD_REQUEST,
    TOO_MANY_REQUESTS,
    FORBIDDEN,
    NOT_FOUND,
    // server errors
    INTERNAL_ERROR,
    NOT_IMPLEMENTED
  }
}
