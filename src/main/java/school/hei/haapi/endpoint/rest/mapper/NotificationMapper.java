package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Notification;
import school.hei.haapi.endpoint.rest.model.NotificationResolutionStatus;

@Component
public class NotificationMapper {

  public Notification toRest(school.hei.haapi.model.Notification domain) {
    return new Notification()
        .id(domain.getId())
        .recipientId(domain.getRecipient().getId())
        .subject(domain.getSubject())
        .body(domain.getBody())
        .read(domain.isRead())
        .resolutionStatus(toRest(domain.getResolutionStatus()))
        .smsCampaignId(domain.getSmsCampaign() == null ? null : domain.getSmsCampaign().getId())
        .creationDatetime(domain.getCreationDatetime());
  }

  public NotificationResolutionStatus toRest(
      school.hei.haapi.model.NotificationResolutionStatus domain) {
    return domain == null ? null : NotificationResolutionStatus.valueOf(domain.name());
  }

  public school.hei.haapi.model.NotificationResolutionStatus toDomain(
      NotificationResolutionStatus rest) {
    return rest == null
        ? null
        : school.hei.haapi.model.NotificationResolutionStatus.valueOf(rest.name());
  }
}
