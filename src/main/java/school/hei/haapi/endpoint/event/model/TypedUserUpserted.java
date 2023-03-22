package school.hei.haapi.endpoint.event.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import school.hei.haapi.endpoint.event.model.gen.UserUpserted;

@AllArgsConstructor
public class TypedUserUpserted implements TypedEvent {

  private final UserUpserted userUpserted;

  @Override
  public String getTypeName() {
    return UserUpserted.class.getTypeName();
  }

  @Override
  public Serializable getPayload() {
    return userUpserted;
  }
}
