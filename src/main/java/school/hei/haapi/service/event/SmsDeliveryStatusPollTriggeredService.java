package school.hei.haapi.service.event;

import java.time.Instant;
import java.util.HashSet;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import school.hei.haapi.endpoint.event.model.SmsDeliveryStatusPollTriggered;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsMessageStatus;
import school.hei.haapi.repository.SmsCampaignRepository;
import school.hei.haapi.repository.SmsLogRepository;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.befiana.BefianaException;

@Slf4j
@org.springframework.stereotype.Service
@AllArgsConstructor
public class SmsDeliveryStatusPollTriggeredService
    implements Consumer<SmsDeliveryStatusPollTriggered> {
  private final SmsLogRepository smsLogRepository;
  private final SmsCampaignRepository smsCampaignRepository;
  private final BefianaClient befianaClient;

  @Override
  public void accept(SmsDeliveryStatusPollTriggered event) {
    var pending =
        smsLogRepository.findAllByStatusAndCallbackDataIsNotNull(SmsMessageStatus.PENDING);
    if (pending.isEmpty()) {
      return;
    }
    log.info("Polling BEFIANA delivery status for {} pending SMS", pending.size());

    var touchedCampaigns = new HashSet<SmsCampaign>();
    for (var pendingLog : pending) {
      try {
        var response = befianaClient.getDeliveryStatus(pendingLog.getCallbackData());
        if ("Delivered".equalsIgnoreCase(response.getDeliveryStatus())) {
          pendingLog.setStatus(SmsMessageStatus.DELIVERED);
          pendingLog.setDeliveredDatetime(Instant.now());
          smsLogRepository.save(pendingLog);
          touchedCampaigns.add(pendingLog.getCampaign());
        }
      } catch (BefianaException e) {
        log.warn(
            "Could not poll delivery status for SmsLog {}: {}", pendingLog.getId(), e.getMessage());
      }
    }
    touchedCampaigns.forEach(this::updateDeliveredCountIfFullyResolved);
  }

  private void updateDeliveredCountIfFullyResolved(SmsCampaign campaign) {
    var stillPending =
        smsLogRepository.findAllByCampaign_IdAndStatusOrderBySentDatetimeDesc(
            campaign.getId(), SmsMessageStatus.PENDING);
    if (!stillPending.isEmpty()) {
      return;
    }
    var deliveredCount =
        smsLogRepository.countByCampaign_IdAndStatus(campaign.getId(), SmsMessageStatus.DELIVERED);
    campaign.setDeliveredCount((int) deliveredCount);
    smsCampaignRepository.save(campaign);
  }
}
