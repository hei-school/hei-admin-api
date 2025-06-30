package school.hei.haapi.service;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.Mpbs.Mpbs;

@Component
@RequiredArgsConstructor
@Slf4j
public class MobilePaymentUnverifiedHandler implements Consumer<List<Mpbs>> {
  private final MpbsService mpbsService;
  private final FailedMobilePaymentNotification failedMobilePaymentNotification;

  @Override
  public void accept(List<Mpbs> mpbsList) {
    Instant now = now();
    List<Mpbs> verifiedMpbs =
        mpbsList.stream()
            .map(
                actual -> {
                  var mpbs =
                      actual.toBuilder()
                          .lastVerificationDatetime(now)
                          .status(mpbsNewStatus(actual))
                          .build();
                  return mpbs;
                })
            .toList();
    List<Mpbs> failedMpbsList =
        verifiedMpbs.stream().filter(failedMpbs -> FAILED.equals(failedMpbs.getStatus())).toList();

    failedMobilePaymentNotification.accept(failedMpbsList);

    mpbsService.saveAll(verifiedMpbs);
  }

  private MpbsStatus mpbsNewStatus(Mpbs mpbs) {
    long dayValidity = mpbs.getCreationDatetime().until(now(), DAYS);
    if (dayValidity > 2) {
      log.info("failed transaction");
      return FAILED;
    }
    log.info("pending transaction");
    return PENDING;
  }
}
