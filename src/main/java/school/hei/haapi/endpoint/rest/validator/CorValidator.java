package school.hei.haapi.endpoint.rest.validator;

import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateCor;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class CorValidator implements Consumer<CrupdateCor> {
  @Override
  public void accept(CrupdateCor cor) {
    if (cor.getStatus() == null) {
      throw new BadRequestException("Status is mandatory");
    }
  }
}
