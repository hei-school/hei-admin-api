package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.SendLateFeesEmailTriggered;
import school.hei.haapi.service.FeeService;

@Service
@AllArgsConstructor
public class SendLateFeesEmailTriggeredService implements Consumer<SendLateFeesEmailTriggered> {

  private final FeeService feeService;

  @Override
  public void accept(SendLateFeesEmailTriggered sendLateFeesEmailTriggered) {
    feeService.sendLateFeesEmail();
  }
}
