package school.hei.haapi.service;

import static java.time.Instant.now;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.MOBILE_MONEY;

import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.PaidFeeByMpbsFailedNotificationBody;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.Payment;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailedMobilePaymentNotification implements Consumer<List<Mpbs>> {
  private final EventProducer eventProducer;

  @Override
  public void accept(List<Mpbs> failedMobilePayments) {
    List<PaidFeeByMpbsFailedNotificationBody> notificationBodyList =
        failedMobilePayments.stream()
            .map(
                mpbs -> {
                  log.info(
                      "Fail verification {} for student {}",
                      mpbs.getId(),
                      mpbs.getStudent().getId());
                  return PaidFeeByMpbsFailedNotificationBody.from(
                      Payment.builder()
                          .type(MOBILE_MONEY)
                          .fee(mpbs.getFee())
                          .amount(mpbs.getAmount())
                          .creationDatetime(now())
                          .comment(mpbs.getFee().getComment())
                          .build());
                })
            .toList();

    eventProducer.accept(notificationBodyList);
  }
}
