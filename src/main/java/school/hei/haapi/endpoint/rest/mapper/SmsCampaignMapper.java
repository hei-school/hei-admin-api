package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.SmsCampaign;
import school.hei.haapi.endpoint.rest.model.SmsCampaignLaunched;
import school.hei.haapi.endpoint.rest.model.SmsCampaignStatus;
import school.hei.haapi.endpoint.rest.model.SmsFileImportRejectedRow;
import school.hei.haapi.model.SmsContactGroup;

@Component
public class SmsCampaignMapper {

  public SmsCampaign toRest(school.hei.haapi.model.SmsCampaign domain) {
    return new SmsCampaign()
        .id(domain.getId())
        .message(domain.getMessage())
        .status(toRest(domain.getStatus()))
        .failureReason(domain.getFailureReason())
        .contactGroupIds(domain.getContactGroups().stream().map(SmsContactGroup::getId).toList())
        .contactGroupNames(
            domain.getContactGroups().stream().map(SmsContactGroup::getName).toList())
        .contactIds(
            domain.getContacts().stream().map(school.hei.haapi.model.SmsContact::getId).toList())
        .manualPhoneNumberCount(domain.getManualPhoneNumbers().size())
        .fileImportCount(domain.getFileImportCount())
        .recipientCount(domain.getRecipientCount())
        .recipientsRejectedForBalance(domain.getRecipientsRejectedForBalance())
        .deliveredCount(domain.getDeliveredCount())
        .failedCount(domain.getFailedCount())
        .smsSegmentsEach(domain.getSmsSegmentsEach())
        .creditsDebited(domain.getCreditsDebited())
        .sendAt(domain.getSendAt())
        .createdById(domain.getCreatedBy() == null ? null : domain.getCreatedBy().getId())
        .createdByRef(domain.getCreatedBy() == null ? null : domain.getCreatedBy().getRef())
        .createdByFirstName(
            domain.getCreatedBy() == null ? null : domain.getCreatedBy().getFirstName())
        .creationDatetime(domain.getCreationDatetime());
  }

  public SmsCampaignLaunched toLaunched(
      school.hei.haapi.model.SmsCampaign domain, List<SmsFileImportRejectedRow> rejectedRows) {
    return new SmsCampaignLaunched()
        .campaignId(domain.getId())
        .recipientCount(domain.getRecipientCount())
        .recipientsRejected(rejectedRows.size())
        .rejectedRows(rejectedRows)
        .recipientsRejectedForBalance(domain.getRecipientsRejectedForBalance())
        .smsSegmentsEach(domain.getSmsSegmentsEach())
        .creditsDebited(domain.getCreditsDebited())
        .status(toRest(domain.getStatus()));
  }

  public SmsCampaignStatus toRest(school.hei.haapi.model.SmsCampaignStatus domain) {
    return domain == null ? null : SmsCampaignStatus.valueOf(domain.name());
  }

  public school.hei.haapi.model.SmsCampaignStatus toDomain(SmsCampaignStatus rest) {
    return rest == null ? null : school.hei.haapi.model.SmsCampaignStatus.valueOf(rest.name());
  }
}
