package school.hei.haapi.service.event;

import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import school.hei.haapi.endpoint.event.model.SmsDeliveryStatusPollTriggered;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignBatch;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.model.SmsLog;
import school.hei.haapi.model.SmsMessageStatus;
import school.hei.haapi.repository.SmsCampaignRepository;
import school.hei.haapi.repository.SmsLogRepository;
import school.hei.haapi.service.befiana.BefianaBatchMessagesPage;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.befiana.BefianaException;

@Slf4j
@org.springframework.stereotype.Service
@AllArgsConstructor
public class SmsDeliveryStatusPollTriggeredService
    implements Consumer<SmsDeliveryStatusPollTriggered> {
  private static final List<SmsMessageStatus> NON_TERMINAL_STATUSES =
      List.of(
          SmsMessageStatus.SCHEDULED,
          SmsMessageStatus.PROCESSING,
          SmsMessageStatus.ACCEPTED,
          SmsMessageStatus.UNKNOWN);
  private static final List<SmsMessageStatus> FAILURE_STATUSES =
      List.of(SmsMessageStatus.UNDELIVERABLE, SmsMessageStatus.BLOCKED, SmsMessageStatus.FAILED);

  private final SmsLogRepository smsLogRepository;
  private final SmsCampaignRepository smsCampaignRepository;
  private final BefianaClient befianaClient;

  @Override
  public void accept(SmsDeliveryStatusPollTriggered event) {
    var pendingLogs = smsLogRepository.findAllByStatusIn(NON_TERMINAL_STATUSES);
    if (pendingLogs.isEmpty()) {
      return;
    }
    log.info("Polling BEFIANA delivery status for {} pending SMS", pendingLogs.size());

    var touchedCampaigns = new HashSet<SmsCampaign>();

    pendingLogs.stream()
        .filter(smsLog -> smsLog.getCampaignBatch() != null)
        .collect(Collectors.groupingBy(SmsLog::getCampaignBatch))
        .forEach(
            (campaignBatch, logs) -> {
              pollBatch(campaignBatch, logs);
              touchedCampaigns.add(campaignBatch.getCampaign());
            });

    pendingLogs.stream()
        .filter(smsLog -> smsLog.getCampaignBatch() == null && smsLog.getBefianaMessageId() != null)
        .forEach(
            smsLog -> {
              pollUnitary(smsLog);
              touchedCampaigns.add(smsLog.getCampaign());
            });

    touchedCampaigns.forEach(this::resolveCampaignIfFullyTerminal);
  }

  private void pollBatch(SmsCampaignBatch campaignBatch, List<SmsLog> localLogs) {
    var byPhoneNumber =
        localLogs.stream()
            .collect(Collectors.toMap(SmsLog::getPhoneNumber, smsLog -> smsLog, (a, b) -> a));
    String cursor = null;
    do {
      BefianaBatchMessagesPage page;
      try {
        page = befianaClient.getBatchMessages(campaignBatch.getBefianaBatchId(), cursor);
      } catch (BefianaException e) {
        log.warn(
            "Could not poll BEFIANA batch {} (campaign {}): {}",
            campaignBatch.getBefianaBatchId(),
            campaignBatch.getCampaign().getId(),
            e.getMessage());
        return;
      }
      for (var message : page.getResults()) {
        var smsLog = byPhoneNumber.get(message.getTo());
        if (smsLog == null) {
          continue;
        }
        smsLog.setBefianaMessageId(message.getId());
        smsLog.setStatus(SmsMessageStatus.fromBefiana(message.getStatus()));
        if (message.getDeliveryTime() != null) {
          smsLog.setDeliveredDatetime(message.getDeliveryTime());
        }
        smsLogRepository.save(smsLog);
      }
      cursor = page.getNextCursor();
    } while (cursor != null);
  }

  private void pollUnitary(SmsLog smsLog) {
    try {
      var message = befianaClient.getMessage(smsLog.getBefianaMessageId());
      smsLog.setStatus(SmsMessageStatus.fromBefiana(message.getStatus()));
      if (message.getDeliveryTime() != null) {
        smsLog.setDeliveredDatetime(message.getDeliveryTime());
      }
      smsLogRepository.save(smsLog);
    } catch (BefianaException e) {
      log.warn(
          "Could not poll BEFIANA message {} for SmsLog {}: {}",
          smsLog.getBefianaMessageId(),
          smsLog.getId(),
          e.getMessage());
    }
  }

  private void resolveCampaignIfFullyTerminal(SmsCampaign campaign) {
    var stillPending =
        smsLogRepository.countByCampaign_IdAndStatusIn(campaign.getId(), NON_TERMINAL_STATUSES);
    if (stillPending > 0) {
      return;
    }
    var deliveredCount =
        smsLogRepository.countByCampaign_IdAndStatus(campaign.getId(), SmsMessageStatus.DELIVERED);
    var failedCount =
        smsLogRepository.countByCampaign_IdAndStatusIn(campaign.getId(), FAILURE_STATUSES);
    campaign.setDeliveredCount((int) deliveredCount);
    campaign.setFailedCount((int) failedCount);
    campaign.setStatus(deliveredCount > 0 ? SmsCampaignStatus.DELIVERED : SmsCampaignStatus.FAILED);
    smsCampaignRepository.save(campaign);
  }
}
