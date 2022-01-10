package school.hei.haapi.endpoint.event;

import java.io.Serializable;

public class Event implements Serializable {
  public String getType() {
    return this.getClass().getTypeName();
  }
}
