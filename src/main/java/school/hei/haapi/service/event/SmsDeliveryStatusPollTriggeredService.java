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
import school.hei.haapi.service.sms.NotificationService;

@Slf4j
@org.springframework.stereotype.Service
@AllArgsConstructor
public class SmsDeliveryStatusPollTriggeredService
    implements Consumer<SmsDeliveryStatusPollTriggered> {
  private final SmsLogRepository smsLogRepository;
  private final SmsCampaignRepository smsCampaignRepository;
  private final BefianaClient befianaClient;
  private final NotificationService notificationService;

  @Override
  public void accept(SmsDeliveryStatusPollTriggered event) {
    var pending =
        smsLogRepository.findAllByStatusAndCallbackDataIsNotNull(SmsMessageStatus.PENDING);
    if (pending.isEmpty()) {
      return;
    }
    log.info("Polling BEFIANA delivery status for {} pending SMS", pending.size());

    var touchedCampaigns = new HashSet<SmsCampaign>();
    for (var l : pending) {
      try {
        var response = befianaClient.getDeliveryStatus(l.getCallbackData());
        if ("Delivered".equalsIgnoreCase(response.getDeliveryStatus())) {
          l.setStatus(SmsMessageStatus.DELIVERED);
          l.setDeliveredDatetime(Instant.now());
          smsLogRepository.save(l);
          touchedCampaigns.add(l.getCampaign());
        }
      } catch (BefianaException e) {
        log.warn("Could not poll delivery status for SmsLog {}: {}", l.getId(), e.getMessage());
      }
    }
    touchedCampaigns.forEach(this::notifyIfFullyResolved);
  }

  private void notifyIfFullyResolved(SmsCampaign campaign) {
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

    notificationService.notifyAdminsOfCampaignOutcome(
        campaign,
        "[SMS] Campagne confirmée délivrée : " + campaign.getId(),
        "<p>Tous les destinataires trackables de la campagne (%d) ont été confirmés délivrés par BEFIANA.</p>"
            .formatted(deliveredCount));
  }
}
