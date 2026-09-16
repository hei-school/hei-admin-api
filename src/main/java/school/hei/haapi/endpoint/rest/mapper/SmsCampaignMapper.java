package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.SmsCampaign;
import school.hei.haapi.endpoint.rest.model.SmsCampaignLaunched;
import school.hei.haapi.endpoint.rest.model.SmsCampaignStatus;
import school.hei.haapi.endpoint.rest.model.SmsFileImportRejectedRow;
import school.hei.haapi.model.SmsContactGroup;

@Component
public class SmsCampaignMapper {

  /**
   * contactGroupIds/contactGroupNames/contactIds are derived from the real relations
   * (sms_campaign_contact_group / sms_campaign_contact) at read time — never denormalized on the
   * entity itself, per the feedback that a comma-joined VARCHAR was the wrong call.
   */
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
        .creationDatetime(domain.getCreationDatetime());
  }

  public SmsCampaignLaunched toLaunched(
      school.hei.haapi.model.SmsCampaign domain,
      java.util.List<SmsFileImportRejectedRow> rejectedRows) {
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
