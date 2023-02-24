package school.hei.haapi.endpoint.event.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.ToString;
import school.hei.haapi.endpoint.event.model.gen.LateFeeChecked;

@AllArgsConstructor
@ToString
public class TypedLateFeeChecked implements TypedEvent {
  private final LateFeeChecked lateFeeChecked;

  @Override
  public String getTypeName() {
    return LateFeeChecked.class.getTypeName();
  }

  @Override
  public Serializable getPayload() {
    return lateFeeChecked;
  }
}
