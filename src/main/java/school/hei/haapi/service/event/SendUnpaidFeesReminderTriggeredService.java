package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.gen.SendUnpaidFeesReminderTriggered;
import school.hei.haapi.service.FeeService;

@Service
@AllArgsConstructor
public class SendUnpaidFeesReminderTriggeredService
    implements Consumer<SendUnpaidFeesReminderTriggered> {

  private final FeeService feeService;

  @Override
  public void accept(SendUnpaidFeesReminderTriggered sendUnpaidFeesReminderTriggered) {
    feeService.sendUnpaidFeesEmail();
  }
}
