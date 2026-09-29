package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.SmsLog;
import school.hei.haapi.endpoint.rest.model.SmsMessageStatus;
import school.hei.haapi.endpoint.rest.model.SmsRecipientSource;

@Component
public class SmsLogMapper {

  public SmsLog toRest(school.hei.haapi.model.SmsLog domain) {
    return new SmsLog()
        .id(domain.getId())
        .campaignId(domain.getCampaign().getId())
        .phoneNumber(domain.getPhoneNumber())
        .status(toRest(domain.getStatus()))
        .recipientSource(toRest(domain.getRecipientSource()))
        .contactId(domain.getContact() == null ? null : domain.getContact().getId())
        .sentDatetime(domain.getSentDatetime())
        .deliveredDatetime(domain.getDeliveredDatetime())
        .callbackData(domain.getCallbackData())
        .failureReason(domain.getFailureReason());
  }

  public SmsMessageStatus toRest(school.hei.haapi.model.SmsMessageStatus domain) {
    return domain == null ? null : SmsMessageStatus.valueOf(domain.name());
  }

  public school.hei.haapi.model.SmsMessageStatus toDomain(SmsMessageStatus rest) {
    return rest == null ? null : school.hei.haapi.model.SmsMessageStatus.valueOf(rest.name());
  }

  public SmsRecipientSource toRest(school.hei.haapi.model.SmsRecipientSource domain) {
    return domain == null ? null : SmsRecipientSource.valueOf(domain.name());
  }
}
