package school.hei.haapi.service;

import static java.time.Instant.now;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.model.Mpbs.Mpbs;

@Component
@AllArgsConstructor
@Slf4j
public class UnverifiedMobilePaymentHandler implements Consumer<List<Mpbs>> {
  private final MpbsService mpbsService;
  private final FailedMobilePaymentNotification failedMobilePaymentNotification;

  @Override
  public void accept(List<Mpbs> mpbsList) {
    Instant now = now();
    List<Mpbs> verifiedMpbs =
        mpbsList.stream().map(actual -> updateMpbsInformation(actual, now)).toList();
    List<Mpbs> failedMpbsList =
        verifiedMpbs.stream().filter(mpbs -> FAILED.equals(mpbs.getStatus())).toList();

    failedMobilePaymentNotification.accept(failedMpbsList);

    mpbsService.saveAll(verifiedMpbs);
  }

  private Mpbs updateMpbsInformation(Mpbs mpbs, Instant now) {
    if (SUCCESS.equals(mpbs.getStatus())) {
      log.warn(
          "Update mpbs status failed, mpbs: {} is already successfully verified", mpbs.getId());
      return mpbs;
    }
    return mpbs.toBuilder().lastVerificationDatetime(now).status(mpbsNewStatus(mpbs)).build();
  }

  private MpbsStatus mpbsNewStatus(Mpbs mpbs) {
    if (mpbs.exceedsValidationDate()) {
      log.info("failed transaction: {} with reference: {}", mpbs.getId(), mpbs.getPspId());
      return FAILED;
    }
    log.info("pending transaction: {} with reference: {}", mpbs.getId(), mpbs.getPspId());
    return PENDING;
  }
}
