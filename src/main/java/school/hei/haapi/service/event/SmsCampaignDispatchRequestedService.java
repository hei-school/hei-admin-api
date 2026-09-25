package school.hei.haapi.service.event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import school.hei.haapi.endpoint.event.model.SmsCampaignDispatchRequested;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.model.SmsLog;
import school.hei.haapi.model.SmsMessageStatus;
import school.hei.haapi.repository.SmsCampaignRepository;
import school.hei.haapi.repository.SmsLogRepository;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.befiana.BefianaException;
import school.hei.haapi.service.sms.SmsSegmentCounter;

@Slf4j
@org.springframework.stereotype.Service
@AllArgsConstructor
public class SmsCampaignDispatchRequestedService implements Consumer<SmsCampaignDispatchRequested> {
  private static final int BULK_CHUNK_SIZE = 200;

  private final SmsCampaignRepository smsCampaignRepository;
  private final SmsLogRepository smsLogRepository;
  private final BefianaClient befianaClient;
  private final SmsSegmentCounter smsSegmentCounter;

  @Override
  public void accept(SmsCampaignDispatchRequested event) {
    var campaign = smsCampaignRepository.findById(event.getCampaignId()).orElse(null);
    if (campaign == null) {
      log.warn("SMS campaign {} not found, dropping dispatch event", event.getCampaignId());
      return;
    }
    if (isAlreadyDispatched(campaign)) {
      log.info(
          "SMS campaign {} already dispatched (status={}), skipping",
          campaign.getId(),
          campaign.getStatus());
      return;
    }

    campaign.setStatus(SmsCampaignStatus.PENDING);
    smsCampaignRepository.save(campaign);

    var pendingLogs =
        smsLogRepository.findAllByCampaign_IdOrderBySentDatetimeDesc(campaign.getId());
    var newlyRejected = dropUnaffordable(campaign, pendingLogs);

    var personalized = new ArrayList<SmsLog>();
    var shared = new ArrayList<SmsLog>();
    for (var l : pendingLogs) {
      (l.getPersonalizedMessage() != null ? personalized : shared).add(l);
    }

    var submittedCount = 0;
    for (var l : personalized) {
      if (sendUnitary(l, l.getPersonalizedMessage())) {
        submittedCount++;
      }
    }
    submittedCount += dispatchShared(campaign, shared);

    finalizeCampaign(campaign, newlyRejected, submittedCount);
  }

  private boolean isAlreadyDispatched(SmsCampaign campaign) {
    return campaign.getStatus() == SmsCampaignStatus.DELIVERED
        || campaign.getStatus() == SmsCampaignStatus.FAILED;
  }

  private int dropUnaffordable(SmsCampaign campaign, List<SmsLog> logs) {
    var availableBalance = befianaClient.getBalance();
    var runningCost = 0;
    var toDrop = new ArrayList<SmsLog>();
    for (var l : logs) {
      var segments = smsSegmentCounter.countSegments(applicableMessage(campaign, l));
      if (runningCost + segments > availableBalance) {
        toDrop.add(l);
        continue;
      }
      runningCost += segments;
    }
    logs.removeAll(toDrop);
    smsLogRepository.deleteAll(toDrop);
    if (!toDrop.isEmpty()) {
      campaign.setRecipientsRejectedForBalance(
          campaign.getRecipientsRejectedForBalance() + toDrop.size());
      log.warn(
          "SMS campaign {}: balance dropped to {} by dispatch time, {} recipient(s) newly"
              + " unaffordable",
          campaign.getId(),
          availableBalance,
          toDrop.size());
    }
    return toDrop.size();
  }

  private String applicableMessage(SmsCampaign campaign, SmsLog log) {
    return log.getPersonalizedMessage() != null
        ? log.getPersonalizedMessage()
        : campaign.getMessage();
  }

  private boolean sendUnitary(SmsLog l, String message) {
    try {
      var response = befianaClient.send(l.getPhoneNumber(), message);
      l.setCallbackData(response.getCallbackData());
      l.setSentDatetime(Instant.now());
      l.setStatus(checkDeliveryStatus(response.getCallbackData()));
      if (l.getStatus() == SmsMessageStatus.DELIVERED) {
        l.setDeliveredDatetime(Instant.now());
      }
      smsLogRepository.save(l);
      return true;
    } catch (BefianaException e) {
      log.warn(
          "BEFIANA /send/ failed for a recipient of campaign {}: {}",
          l.getCampaign().getId(),
          e.getMessage());
      l.setStatus(SmsMessageStatus.FAILED);
      l.setSentDatetime(Instant.now());
      l.setFailureReason(e.getMessage());
      smsLogRepository.save(l);
      return false;
    }
  }

  private SmsMessageStatus checkDeliveryStatus(String callbackData) {
    try {
      var status = befianaClient.getDeliveryStatus(callbackData);
      return "Delivered".equalsIgnoreCase(status.getDeliveryStatus())
          ? SmsMessageStatus.DELIVERED
          : SmsMessageStatus.PENDING;
    } catch (BefianaException e) {
      log.warn(
          "BEFIANA get-delivery-status check failed for callbackData {}: {}",
          callbackData,
          e.getMessage());
      return SmsMessageStatus.PENDING;
    }
  }

  private int dispatchShared(SmsCampaign campaign, List<SmsLog> shared) {
    if (shared.isEmpty()) {
      return 0;
    }
    if (shared.size() == 1) {
      return sendUnitary(shared.get(0), campaign.getMessage()) ? 1 : 0;
    }

    var submitted = 0;
    for (var i = 0; i < shared.size(); i += BULK_CHUNK_SIZE) {
      var chunk = shared.subList(i, Math.min(i + BULK_CHUNK_SIZE, shared.size()));
      submitted += submitChunk(campaign, chunk) ? chunk.size() : 0;
    }
    return submitted;
  }

  private boolean submitChunk(SmsCampaign campaign, List<SmsLog> chunk) {
    var numbers = chunk.stream().map(SmsLog::getPhoneNumber).toList();
    var now = Instant.now();
    try {
      var response = befianaClient.sendBulk(numbers, campaign.getMessage());
      chunk.forEach(
          l -> {
            l.setStatus(SmsMessageStatus.DELIVERED);
            l.setSentDatetime(now);
            l.setDeliveredDatetime(now);
          });
      smsLogRepository.saveAll(chunk);
      campaign.setSmsSegmentsEach(response.getSmsSegmentsEach());
      campaign.setCreditsDebited(
          (campaign.getCreditsDebited() == null ? 0 : campaign.getCreditsDebited())
              + (response.getBalanceDebited() == null ? 0 : response.getBalanceDebited()));
      return true;
    } catch (BefianaException e) {
      log.warn(
          "BEFIANA /sendbulk/ chunk failed for campaign {}: {}", campaign.getId(), e.getMessage());
      chunk.forEach(
          l -> {
            l.setStatus(SmsMessageStatus.FAILED);
            l.setSentDatetime(now);
            l.setFailureReason(e.getMessage());
          });
      smsLogRepository.saveAll(chunk);
      return false;
    }
  }

  private void finalizeCampaign(SmsCampaign campaign, int newlyRejected, int submittedCount) {
    var failedCount =
        smsLogRepository.countByCampaign_IdAndStatus(campaign.getId(), SmsMessageStatus.FAILED);
    campaign.setFailedCount((int) failedCount);
    var deliveredCount =
        smsLogRepository.countByCampaign_IdAndStatus(campaign.getId(), SmsMessageStatus.DELIVERED);
    campaign.setDeliveredCount((int) deliveredCount);

    var nothingWentThrough = submittedCount == 0;
    campaign.setStatus(nothingWentThrough ? SmsCampaignStatus.FAILED : SmsCampaignStatus.DELIVERED);
    if (nothingWentThrough) {
      campaign.setFailureReason(
          campaign.getRecipientsRejectedForBalance() >= campaign.getRecipientCount()
              ? "Solde insuffisant au moment de l'envoi effectif de la campagne."
              : "BEFIANA a rejeté l'envoi pour tous les destinataires.");
    }
    smsCampaignRepository.save(campaign);

    if (nothingWentThrough || newlyRejected > 0 || failedCount > 0) {
      log.warn(
          "SMS campaign {} needs admin attention (nothingWentThrough={}, newlyRejected={},"
              + " failedCount={}) — no notification mechanism wired up yet",
          campaign.getId(),
          nothingWentThrough,
          newlyRejected,
          failedCount);
    }
  }
}
