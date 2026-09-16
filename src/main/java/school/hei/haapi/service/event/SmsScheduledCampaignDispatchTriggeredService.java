package school.hei.haapi.service.event;

import java.time.Instant;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import school.hei.haapi.endpoint.event.model.SmsScheduledCampaignDispatchTriggered;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.repository.SmsCampaignRepository;
import school.hei.haapi.service.sms.SmsCampaignService;

@Slf4j
@org.springframework.stereotype.Service
@AllArgsConstructor
public class SmsScheduledCampaignDispatchTriggeredService
    implements Consumer<SmsScheduledCampaignDispatchTriggered> {
  private final SmsCampaignRepository smsCampaignRepository;
  private final SmsCampaignService smsCampaignService;

  @Override
  public void accept(SmsScheduledCampaignDispatchTriggered event) {
    var due =
        smsCampaignRepository.findAllByStatusAndSendAtLessThanEqual(
            SmsCampaignStatus.CREATED, Instant.now());
    if (due.isEmpty()) {
      return;
    }
    log.info("{} scheduled SMS campaign(s) due for dispatch", due.size());
    for (var campaign : due) {
      smsCampaignService.dispatchNow(campaign.getId());
    }
  }
}
