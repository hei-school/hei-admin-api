package school.hei.haapi.service.sms;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.SmsCampaignDispatchRequested;
import school.hei.haapi.endpoint.rest.model.SmsFileImportRejectedRow;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsLog;
import school.hei.haapi.model.SmsRecipientSource;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.exception.SmsInsufficientBalanceException;
import school.hei.haapi.repository.SmsCampaignRepository;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.repository.SmsLogRepository;
import school.hei.haapi.repository.dao.SmsCampaignDao;
import school.hei.haapi.service.befiana.BefianaClient;

@Slf4j
@org.springframework.stereotype.Service
@AllArgsConstructor
public class SmsCampaignService {
  private final SmsCampaignRepository smsCampaignRepository;
  private final SmsCampaignDao smsCampaignDao;
  private final SmsLogRepository smsLogRepository;
  private final SmsContactRepository smsContactRepository;
  private final SmsRecipientResolver smsRecipientResolver;
  private final SmsSegmentCounter smsSegmentCounter;
  private final BefianaClient befianaClient;
  private final EventProducer<SmsCampaignDispatchRequested> dispatchEventProducer;

  private record CostedRecipient(ResolvedRecipient recipient, int segments) {}

  public record CreationResult(SmsCampaign campaign, List<SmsFileImportRejectedRow> rejectedRows) {}

  public record CreateSmsCampaignCommand(
      User createdBy,
      String message,
      List<String> contactGroupIds,
      List<String> contactIds,
      List<String> manualPhoneNumbers,
      File file,
      String originalFilename,
      Instant sendAt) {}

  @Transactional
  public CreationResult createCampaign(CreateSmsCampaignCommand command) {
    var resolved =
        smsRecipientResolver.resolve(
            command.contactGroupIds(),
            command.contactIds(),
            command.manualPhoneNumbers(),
            command.file(),
            command.originalFilename());

    var sharedSegments = smsSegmentCounter.countSegments(command.message());
    var costed =
        resolved.recipients().stream()
            .map(
                r ->
                    new CostedRecipient(
                        r,
                        r.isPersonalized()
                            ? smsSegmentCounter.countSegments(r.personalizedMessage())
                            : sharedSegments))
            .toList();

    var availableBalance = befianaClient.getBalance();
    var affordable = takeAffordable(costed, availableBalance);

    if (affordable.isEmpty()) {
      throw new SmsInsufficientBalanceException(
          ("Solde insuffisant : %d SMS disponibles pour %d destinataires demandés. Réduisez la"
                  + " liste ou rechargez le compte.")
              .formatted(availableBalance, costed.size()),
          availableBalance,
          costed.size(),
          0);
    }

    var manualNumbers =
        costed.stream()
            .map(CostedRecipient::recipient)
            .filter(r -> r.source() == SmsRecipientSource.MANUAL_NUMBER)
            .map(ResolvedRecipient::phoneNumber)
            .toList();

    var campaign =
        smsCampaignRepository.save(
            SmsCampaign.builder()
                .id(UUID.randomUUID().toString())
                .message(command.message())
                .status(SmsCampaignStatus.CREATED)
                .contactGroups(resolved.contactGroups())
                .contacts(resolved.manuallySelectedContacts())
                .manualPhoneNumbers(manualNumbers)
                .fileImportCount(resolved.fileImportCount())
                .fileBucketKey(resolved.fileBucketKey())
                .recipientCount(costed.size())
                .recipientsRejectedForBalance(costed.size() - affordable.size())
                .smsSegmentsEach(sharedSegments)
                .sendAt(command.sendAt())
                .createdBy(command.createdBy())
                .build());

    var contactsById =
        smsContactRepository
            .findAllById(
                affordable.stream()
                    .map(c -> c.recipient().contactId())
                    .filter(Objects::nonNull)
                    .toList())
            .stream()
            .collect(Collectors.toMap(SmsContact::getId, c -> c));

    for (var c : affordable) {
      smsLogRepository.save(
          SmsLog.builder()
              .id(UUID.randomUUID().toString())
              .campaign(campaign)
              .phoneNumber(c.recipient().phoneNumber())
              .recipientSource(c.recipient().source())
              .contact(contactsById.get(c.recipient().contactId()))
              .personalizedMessage(c.recipient().personalizedMessage())
              .build());
    }

    var sendAt = command.sendAt();
    if (sendAt == null || !sendAt.isAfter(Instant.now())) {
      dispatchNow(campaign.getId());
    }

    return new CreationResult(campaign, resolved.rejectedRows());
  }

  private List<CostedRecipient> takeAffordable(List<CostedRecipient> costed, int availableBalance) {
    var affordable = new ArrayList<CostedRecipient>();
    var runningCost = 0;
    for (var c : costed) {
      if (runningCost + c.segments() > availableBalance) {
        break;
      }
      runningCost += c.segments();
      affordable.add(c);
    }
    return affordable;
  }

  public void dispatchNow(String campaignId) {
    dispatchEventProducer.accept(List.of(new SmsCampaignDispatchRequested(campaignId)));
  }

  public SmsCampaign getById(String id) {
    return smsCampaignRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("SMS campaign " + id + " not found"));
  }

  public List<SmsCampaign> getByCriteria(SmsCampaignStatus status, Pageable pageable) {
    return smsCampaignDao.filterByCriteria(status, pageable);
  }

  public SmsCampaign save(SmsCampaign campaign) {
    return smsCampaignRepository.save(campaign);
  }
}
