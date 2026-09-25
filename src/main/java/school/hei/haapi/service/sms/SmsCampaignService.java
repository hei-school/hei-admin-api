package school.hei.haapi.service.sms;

import java.io.File;
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

  public record CreationResult(SmsCampaign campaign) {}

  public record CreateSmsCampaignCommand(
      User createdBy,
      String message,
      List<String> contactGroupIds,
      List<String> contactIds,
      List<String> manualPhoneNumbers,
      File file,
      String originalFilename) {}

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
                recipient ->
                    new CostedRecipient(
                        recipient,
                        recipient.isPersonalized()
                            ? smsSegmentCounter.countSegments(recipient.personalizedMessage())
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
            .filter(recipient -> recipient.source() == SmsRecipientSource.MANUAL_NUMBER)
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
                .createdBy(command.createdBy())
                .build());

    var contactsById =
        smsContactRepository
            .findAllById(
                affordable.stream()
                    .map(costedRecipient -> costedRecipient.recipient().contactId())
                    .filter(Objects::nonNull)
                    .toList())
            .stream()
            .collect(Collectors.toMap(SmsContact::getId, contact -> contact));

    for (var costedRecipient : affordable) {
      smsLogRepository.save(
          SmsLog.builder()
              .id(UUID.randomUUID().toString())
              .campaign(campaign)
              .phoneNumber(costedRecipient.recipient().phoneNumber())
              .recipientSource(costedRecipient.recipient().source())
              .contact(contactsById.get(costedRecipient.recipient().contactId()))
              .personalizedMessage(costedRecipient.recipient().personalizedMessage())
              .build());
    }

    dispatchNow(campaign.getId());

    return new CreationResult(campaign);
  }

  private List<CostedRecipient> takeAffordable(List<CostedRecipient> costed, int availableBalance) {
    var affordable = new ArrayList<CostedRecipient>();
    var runningCost = 0;
    for (var costedRecipient : costed) {
      if (runningCost + costedRecipient.segments() > availableBalance) {
        break;
      }
      runningCost += costedRecipient.segments();
      affordable.add(costedRecipient);
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
