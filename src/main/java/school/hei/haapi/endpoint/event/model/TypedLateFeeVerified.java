package school.hei.haapi.endpoint.event.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.ToString;
import school.hei.haapi.endpoint.event.model.gen.LateFeeVerified;

@AllArgsConstructor
@ToString
public class TypedLateFeeVerified implements TypedEvent {

  private final LateFeeVerified lateFeeVerified;

  @Override
  public String getTypeName() {
    return LateFeeVerified.class.getTypeName();
  }

  @Override
  public Serializable getPayload() {
    return lateFeeVerified;
  }
}
