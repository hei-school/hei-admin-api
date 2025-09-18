package school.hei.haapi.service;

import static java.time.Instant.now;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.MOBILE_MONEY;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.PaidFeeByMpbsFailedNotificationBody;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.mpbs.Mpbs;

@Slf4j
@Component
@AllArgsConstructor
public class FailedMobilePaymentNotification implements Consumer<List<Mpbs>> {
  private final EventProducer<PaidFeeByMpbsFailedNotificationBody> eventProducer;

  @Override
  public void accept(List<Mpbs> failedMobilePayments) {
    var notificationBodyList =
        failedMobilePayments.stream()
            .map(this::validMpbs)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    eventProducer.accept(notificationBodyList);
  }

  private Optional<PaidFeeByMpbsFailedNotificationBody> validMpbs(Mpbs mpbs) {
    try {
      log.info("Fail verification {} for student {}", mpbs.getId(), mpbs.getStudent().getId());
      return Optional.of(
          PaidFeeByMpbsFailedNotificationBody.from(
              Payment.builder()
                  .type(MOBILE_MONEY)
                  .fee(mpbs.getFee())
                  .amount(mpbs.getAmount())
                  .creationDatetime(now())
                  .comment(mpbs.getFee().getComment())
                  .build()));
    } catch (Exception e) {
      log.error("Bad mpbs", e);
      return Optional.empty();
    }
  }
}
