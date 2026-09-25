package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.SmsLogMapper;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsLog;

class SmsLogMapperTest {
  private final SmsLogMapper subject = new SmsLogMapper();

  @Test
  void every_domain_message_status_maps_to_its_rest_counterpart_by_name() {
    for (var domainStatus : school.hei.haapi.model.SmsMessageStatus.values()) {
      var rest = subject.toRest(domainStatus);
      assertEquals(domainStatus.name(), rest.name());
      assertEquals(domainStatus, subject.toDomain(rest));
    }
  }

  @Test
  void every_domain_recipient_source_maps_to_its_rest_counterpart_by_name() {
    for (var domainSource : school.hei.haapi.model.SmsRecipientSource.values()) {
      assertEquals(domainSource.name(), subject.toRest(domainSource).name());
    }
  }

  @Test
  void null_recipient_source_is_null_safe() {
    assertNull(subject.toRest((school.hei.haapi.model.SmsRecipientSource) null));
  }

  @Test
  void a_bulk_sent_log_with_a_null_status_maps_to_null_not_a_default_value() {
    var campaign = SmsCampaign.builder().id("c1").status(SmsCampaignStatus.DELIVERED).build();
    var bulkSentLogWithNoStatusOrCallbackDataYet =
        SmsLog.builder()
            .id("log1")
            .campaign(campaign)
            .phoneNumber("321111111")
            .recipientSource(school.hei.haapi.model.SmsRecipientSource.MANUAL_NUMBER)
            .build();

    var rest = subject.toRest(bulkSentLogWithNoStatusOrCallbackDataYet);

    assertNull(rest.getStatus());
    assertNull(rest.getCallbackData());
    assertNull(rest.getContactId());
  }

  @Test
  void contact_id_is_populated_when_the_recipient_came_from_a_saved_contact() {
    var campaign = SmsCampaign.builder().id("c1").status(SmsCampaignStatus.DELIVERED).build();
    var contact = SmsContact.builder().id("contact1").build();
    var log =
        SmsLog.builder()
            .id("log1")
            .campaign(campaign)
            .phoneNumber("321111111")
            .contact(contact)
            .recipientSource(school.hei.haapi.model.SmsRecipientSource.CONTACT_GROUP)
            .build();

    assertEquals("contact1", subject.toRest(log).getContactId());
  }

  @Test
  void failure_reason_is_carried_through_for_a_failed_log() {
    var campaign = SmsCampaign.builder().id("c1").status(SmsCampaignStatus.FAILED).build();
    var log =
        SmsLog.builder()
            .id("log1")
            .campaign(campaign)
            .phoneNumber("321111111")
            .status(school.hei.haapi.model.SmsMessageStatus.FAILED)
            .failureReason("BEFIANA call to /send/ failed: HTTP 400 - numéro invalide")
            .build();

    assertEquals(
        "BEFIANA call to /send/ failed: HTTP 400 - numéro invalide",
        subject.toRest(log).getFailureReason());
  }
}
