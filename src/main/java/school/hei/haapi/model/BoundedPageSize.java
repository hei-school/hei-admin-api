package school.hei.haapi.model;

import lombok.Getter;
import school.hei.haapi.model.exception.BadRequestException;

public class BoundedPageSize {

  @Getter
  private final int value;

  private static final int MAX_SIZE = 500;

  public BoundedPageSize(int value) {
    if (value > MAX_SIZE) {
      throw new BadRequestException("page size must be <" + MAX_SIZE);
    }
    this.value = value;
  }
}
