package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.controller.SmsContactController;
import school.hei.haapi.endpoint.rest.mapper.SmsCampaignMapper;
import school.hei.haapi.endpoint.rest.mapper.SmsContactMapper;
import school.hei.haapi.endpoint.rest.model.SendSmsToContact;
import school.hei.haapi.endpoint.rest.model.SmsContactOwnerRole;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.User;
import school.hei.haapi.service.sms.SmsCampaignService;
import school.hei.haapi.service.sms.SmsCampaignService.CreateSmsCampaignCommand;
import school.hei.haapi.service.sms.SmsContactService;

class SmsContactControllerTest {
  private final SmsContactService smsContactServiceMock = mock();
  private final SmsContactMapper smsContactMapper = new SmsContactMapper();
  private final SmsCampaignService smsCampaignServiceMock = mock();
  private final SmsCampaignMapper smsCampaignMapper = new SmsCampaignMapper();

  private final SmsContactController subject =
      new SmsContactController(
          smsContactServiceMock, smsContactMapper, smsCampaignServiceMock, smsCampaignMapper);

  private final User owner = User.builder().id("student1").ref("STD000001").build();
  private final Principal principal = new Principal(User.builder().id("admin1").build(), "token");

  private SmsContact contact() {
    return SmsContact.builder()
        .id("contact1")
        .phoneNumber("321111111")
        .name("Antenaina Jaonina")
        .owner(owner)
        .ownerRole(school.hei.haapi.model.SmsContactOwnerRole.STUDENT)
        .build();
  }

  @Test
  void lists_contacts_converting_the_owner_role_filter_to_the_domain_enum() {
    when(smsContactServiceMock.getByCriteria(
            eq("g1"), eq(school.hei.haapi.model.SmsContactOwnerRole.STUDENT), any()))
        .thenReturn(List.of(contact()));

    var page =
        subject.getSmsContacts(
            "g1", SmsContactOwnerRole.STUDENT, new PageFromOne(1), new BoundedPageSize(10));

    assertEquals(1, page.size());
    assertEquals("contact1", page.get(0).getId());
  }

  @Test
  void lists_contacts_with_no_filters() {
    when(smsContactServiceMock.getByCriteria(isNull(), isNull(), any())).thenReturn(List.of());

    var page = subject.getSmsContacts(null, null, new PageFromOne(1), new BoundedPageSize(10));

    assertEquals(0, page.size());
  }

  @Test
  void gets_a_contact_by_id() {
    when(smsContactServiceMock.getById("contact1")).thenReturn(contact());

    assertEquals("321111111", subject.getSmsContactById("contact1").getPhoneNumber());
  }

  @Test
  void deletes_a_contact() {
    var deleted = contact();
    deleted.setDeleted(true);
    when(smsContactServiceMock.delete("contact1")).thenReturn(deleted);

    assertEquals("contact1", subject.deleteSmsContact("contact1").getId());
  }

  @Test
  void sending_to_a_contact_is_a_one_recipient_campaign() {
    var launchedCampaign =
        SmsCampaign.builder().id("campaign1").status(SmsCampaignStatus.CREATED).build();
    when(smsCampaignServiceMock.createCampaign(any(CreateSmsCampaignCommand.class)))
        .thenReturn(new SmsCampaignService.CreationResult(launchedCampaign, List.of()));
    var toSend = new SendSmsToContact().message("Hello");

    var launched = subject.sendSmsMessageToContact("contact1", toSend, principal);

    assertEquals("campaign1", launched.getCampaignId());
    verify(smsCampaignServiceMock)
        .createCampaign(
            eq(
                new CreateSmsCampaignCommand(
                    principal.getUser(),
                    "Hello",
                    null,
                    List.of("contact1"),
                    null,
                    null,
                    null,
                    null)));
  }
}
