package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.SmsCampaignMapper;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactGroup;
import school.hei.haapi.model.User;

class SmsCampaignMapperTest {
  private final SmsCampaignMapper subject = new SmsCampaignMapper();

  @Test
  void every_domain_status_maps_to_its_rest_counterpart_by_name() {
    for (var domainStatus : school.hei.haapi.model.SmsCampaignStatus.values()) {
      var rest = subject.toRest(domainStatus);
      assertEquals(domainStatus.name(), rest.name());
      assertEquals(domainStatus, subject.toDomain(rest));
    }
  }

  @Test
  void null_status_is_null_safe_both_ways() {
    assertNull(subject.toRest((school.hei.haapi.model.SmsCampaignStatus) null));
    assertNull(subject.toDomain(null));
  }

  @Test
  void contact_group_ids_and_names_are_derived_from_the_relation_not_denormalized() {
    var group = SmsContactGroup.builder().id("g1").name("Promo 2026").build();
    var contact = SmsContact.builder().id("c1").build();
    var campaign =
        school.hei.haapi.model.SmsCampaign.builder()
            .id("campaign1")
            .status(school.hei.haapi.model.SmsCampaignStatus.CREATED)
            .contactGroups(java.util.List.of(group))
            .contacts(java.util.List.of(contact))
            .manualPhoneNumbers(java.util.List.of("321111111", "321111112"))
            .createdBy(User.builder().id("admin1").build())
            .build();

    var rest = subject.toRest(campaign);

    assertEquals(java.util.List.of("g1"), rest.getContactGroupIds());
    assertEquals(java.util.List.of("Promo 2026"), rest.getContactGroupNames());
    assertEquals(java.util.List.of("c1"), rest.getContactIds());
    assertEquals(2, rest.getManualPhoneNumberCount());
    assertEquals("admin1", rest.getCreatedById());
  }

  @Test
  void created_by_is_null_safe_when_the_campaign_has_no_author() {
    var campaign =
        school.hei.haapi.model.SmsCampaign.builder()
            .id("campaign1")
            .status(school.hei.haapi.model.SmsCampaignStatus.CREATED)
            .contactGroups(java.util.List.of())
            .contacts(java.util.List.of())
            .manualPhoneNumbers(java.util.List.of())
            .build();

    assertNull(subject.toRest(campaign).getCreatedById());
  }

  @Test
  void launched_response_bundles_the_campaign_totals() {
    var campaign =
        school.hei.haapi.model.SmsCampaign.builder()
            .id("campaign1")
            .status(school.hei.haapi.model.SmsCampaignStatus.PENDING)
            .recipientCount(5)
            .recipientsRejectedForBalance(1)
            .smsSegmentsEach(2)
            .creditsDebited(10)
            .build();

    var launched = subject.toLaunched(campaign);

    assertEquals("campaign1", launched.getCampaignId());
    assertEquals(5, launched.getRecipientCount());
    assertEquals(1, launched.getRecipientsRejectedForBalance());
    assertEquals(2, launched.getSmsSegmentsEach());
    assertEquals(10, launched.getCreditsDebited());
    assertEquals(
        school.hei.haapi.endpoint.rest.model.SmsCampaignStatus.PENDING, launched.getStatus());
  }
}
