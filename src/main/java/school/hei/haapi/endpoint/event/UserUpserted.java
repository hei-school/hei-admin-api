package school.hei.haapi.endpoint.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class UserUpserted extends Event {
  private String userId;
  private String email;
}
