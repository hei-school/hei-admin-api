package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.SmsContactBackfillTriggered;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.sms.SmsContactService;

@Slf4j
@Service
@AllArgsConstructor
public class SmsContactBackfillTriggeredService implements Consumer<SmsContactBackfillTriggered> {
  private final UserService userService;
  private final SmsContactService smsContactService;

  @Override
  public void accept(SmsContactBackfillTriggered event) {
    var enabledUsers = userService.getAllEnabledUsers();
    var created = 0;
    for (var user : enabledUsers) {
      if (smsContactService.createContactIfMissing(user).isPresent()) {
        created++;
      }
    }
    log.info(
        "SMS contact backfill: {} contact(s) created out of {} enabled user(s)",
        created,
        enabledUsers.size());
  }
}
