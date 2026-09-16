package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.NotificationMapper;
import school.hei.haapi.model.Notification;
import school.hei.haapi.model.User;

class NotificationMapperTest {
  private final NotificationMapper subject = new NotificationMapper();

  @Test
  void every_domain_resolution_status_maps_to_its_rest_counterpart_by_name() {
    for (var domainStatus : school.hei.haapi.model.NotificationResolutionStatus.values()) {
      var rest = subject.toRest(domainStatus);
      assertEquals(domainStatus.name(), rest.name());
      assertEquals(domainStatus, subject.toDomain(rest));
    }
  }

  @Test
  void an_informational_notification_has_no_resolution_status() {
    var notification =
        Notification.builder()
            .id("n1")
            .recipient(User.builder().id("admin1").build())
            .subject("Campagne confirmée délivrée")
            .body("...")
            .read(false)
            .build();

    var rest = subject.toRest(notification);

    assertNull(rest.getResolutionStatus());
    assertNull(rest.getSmsCampaignId());
  }
}
