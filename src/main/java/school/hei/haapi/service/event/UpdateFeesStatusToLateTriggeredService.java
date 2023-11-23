package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.gen.UpdateFeesStatusToLateTriggered;
import school.hei.haapi.service.FeeService;

@Service
@AllArgsConstructor
public class UpdateFeesStatusToLateTriggeredService
    implements Consumer<UpdateFeesStatusToLateTriggered> {
  private final FeeService feeService;

  @Override
  public void accept(UpdateFeesStatusToLateTriggered updateFeesStatusToLate) {
    feeService.updateFeesStatusToLate();
  }
}
