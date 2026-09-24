package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.SmsContactBackfillTriggered;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.sms.SmsContactService;

/**
 * Safety net for SmsContact creation: covers every enabled user who slipped past the on-upsert hook
 * in UserUpsertedService — either because they existed before this feature shipped, or because they
 * were created through a path that never fires UserUpserted (e.g. monitors, see
 * MonitoringStudentService). createContactIfMissing is idempotent, so re-running this over users
 * who already have a contact is harmless.
 */
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
