package school.hei.haapi.endpoint.rest.model;

import lombok.Getter;
import school.hei.haapi.endpoint.rest.model.exception.BadRequestException;

public class PageFromOne {

  @Getter
  private final int value;

  public PageFromOne(int value) {
    if (value < 1) {
      throw new BadRequestException("page value must be >=1");
    }
    this.value = value;
  }
}
