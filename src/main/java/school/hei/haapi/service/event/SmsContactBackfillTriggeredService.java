package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.SmsContactBackfillTriggered;
import school.hei.haapi.service.sms.SmsContactService;

@Service
@AllArgsConstructor
public class SmsContactBackfillTriggeredService implements Consumer<SmsContactBackfillTriggered> {
  private final SmsContactService smsContactService;

  @Override
  public void accept(SmsContactBackfillTriggered event) {
    smsContactService.backfillMissingContacts();
  }
}
