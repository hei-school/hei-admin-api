package school.hei.haapi.handler.model.requestEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Authorizer {
  private IAM iam;
}
