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
import school.hei.haapi.service.sms.NotificationService;
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
  private final NotificationService notificationService;

  @Override
  public void accept(SmsCampaignDispatchRequested event) {
    var campaign = smsCampaignRepository.findById(event.getCampaignId()).orElse(null);
    if (campaign == null) {
      log.warn("SMS campaign {} not found, dropping dispatch event", event.getCampaignId());
      return;
    }
    if (campaign.getStatus() == SmsCampaignStatus.DELIVERED
        || campaign.getStatus() == SmsCampaignStatus.FAILED) {
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

  /**
   * Re-checks balance at dispatch time; drops (deletes) whatever no longer fits, greedily, same
   * order as creation.
   */
  private int dropUnaffordable(SmsCampaign campaign, List<SmsLog> logs) {
    var availableBalance = befianaClient.getBalance();
    var runningCost = 0;
    var toDrop = new ArrayList<SmsLog>();
    for (var l : logs) {
      var segments =
          l.getPersonalizedMessage() != null
              ? smsSegmentCounter.countSegments(l.getPersonalizedMessage())
              : smsSegmentCounter.countSegments(campaign.getMessage());
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

  private boolean sendUnitary(SmsLog l, String message) {
    try {
      var response = befianaClient.send(l.getPhoneNumber(), message, null);
      l.setStatus(SmsMessageStatus.PENDING);
      l.setCallbackData(response.getCallbackData());
      l.setSentDatetime(Instant.now());
      smsLogRepository.save(l);
      return true;
    } catch (BefianaException e) {
      log.warn(
          "BEFIANA /send/ failed for a recipient of campaign {}: {}",
          l.getCampaign().getId(),
          e.getMessage());
      l.setStatus(SmsMessageStatus.FAILED);
      l.setSentDatetime(Instant.now());
      smsLogRepository.save(l);
      return false;
    }
  }

  /** Exactly 1 remaining shared recipient uses /send/ (trackable); 2+ use /sendbulk/, chunked. */
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
      var numbers = chunk.stream().map(SmsLog::getPhoneNumber).toList();
      try {
        var response = befianaClient.sendBulk(numbers, campaign.getMessage(), null);
        var now = Instant.now();
        for (var l : chunk) {
          l.setSentDatetime(now);
        }
        smsLogRepository.saveAll(chunk);
        campaign.setSmsSegmentsEach(response.getSmsSegmentsEach());
        campaign.setCreditsDebited(
            (campaign.getCreditsDebited() == null ? 0 : campaign.getCreditsDebited())
                + (response.getBalanceDebited() == null ? 0 : response.getBalanceDebited()));
        submitted += chunk.size();
      } catch (BefianaException e) {
        log.warn(
            "BEFIANA /sendbulk/ chunk failed for campaign {}: {}",
            campaign.getId(),
            e.getMessage());
        var now = Instant.now();
        for (var l : chunk) {
          l.setStatus(SmsMessageStatus.FAILED);
          l.setSentDatetime(now);
        }
        smsLogRepository.saveAll(chunk);
      }
    }
    return submitted;
  }

  private void finalizeCampaign(SmsCampaign campaign, int newlyRejected, int submittedCount) {
    var failedCount =
        smsLogRepository.countByCampaign_IdAndStatus(campaign.getId(), SmsMessageStatus.FAILED);
    campaign.setFailedCount((int) failedCount);

    var nothingWentThrough = submittedCount == 0;
    if (nothingWentThrough) {
      campaign.setStatus(SmsCampaignStatus.FAILED);
      campaign.setFailureReason(
          campaign.getRecipientsRejectedForBalance() >= campaign.getRecipientCount()
              ? "Solde insuffisant au moment de l'envoi effectif de la campagne."
              : "BEFIANA a rejeté l'envoi pour tous les destinataires.");
    } else {
      campaign.setStatus(SmsCampaignStatus.DELIVERED);
    }
    smsCampaignRepository.save(campaign);

    var needsAdminAttention = nothingWentThrough || newlyRejected > 0 || failedCount > 0;
    if (needsAdminAttention) {
      notifyAdmins(campaign, newlyRejected, failedCount, nothingWentThrough);
    }
  }

  private void notifyAdmins(
      SmsCampaign campaign, int newlyRejected, long failedCount, boolean nothingWentThrough) {
    var subject =
        nothingWentThrough
            ? "[SMS] Campagne non envoyée : " + campaign.getId()
            : "[SMS] Campagne envoyée avec des problèmes : " + campaign.getId();
    var body =
        new StringBuilder()
            .append("<p>Campagne : <b>")
            .append(escapeHtml(campaign.getMessage()))
            .append("</b></p>")
            .append("<p>Destinataires demandés : ")
            .append(campaign.getRecipientCount())
            .append("</p>");
    if (nothingWentThrough) {
      body.append("<p><b>Aucun message n'a pu être envoyé.</b> Raison : ")
          .append(escapeHtml(campaign.getFailureReason()))
          .append("</p>");
    } else {
      if (newlyRejected > 0) {
        body.append("<p>")
            .append(newlyRejected)
            .append(
                " destinataire(s) n'ont pas pu être envoyés : le solde est tombé à zéro entre la"
                    + " création et l'envoi effectif de la campagne programmée.</p>");
      }
      if (failedCount > 0) {
        body.append("<p>")
            .append(failedCount)
            .append(
                " destinataire(s) ont échoué lors de la soumission à BEFIANA (voir les journaux"
                    + " de la campagne).</p>");
      }
    }
    notificationService.notifyAdminsOfCampaignOutcome(campaign, subject, body.toString());
  }

  private String escapeHtml(String s) {
    return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
