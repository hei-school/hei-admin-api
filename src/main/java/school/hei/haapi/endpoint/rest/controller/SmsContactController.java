package school.hei.haapi.endpoint.rest.controller;

import static org.springframework.http.HttpStatus.ACCEPTED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.SmsCampaignMapper;
import school.hei.haapi.endpoint.rest.mapper.SmsContactMapper;
import school.hei.haapi.endpoint.rest.model.SendSmsToContact;
import school.hei.haapi.endpoint.rest.model.SmsCampaignLaunched;
import school.hei.haapi.endpoint.rest.model.SmsContact;
import school.hei.haapi.endpoint.rest.model.SmsContactOwnerRole;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.sms.SmsCampaignService;
import school.hei.haapi.service.sms.SmsContactService;

@RestController
@RequiredArgsConstructor
public class SmsContactController {
  private final SmsContactService smsContactService;
  private final SmsContactMapper smsContactMapper;
  private final SmsCampaignService smsCampaignService;
  private final SmsCampaignMapper smsCampaignMapper;

  @GetMapping("/sms-contacts")
  public List<SmsContact> getSmsContacts(
      @RequestParam(name = "contact_group_id", required = false) String contactGroupId,
      @RequestParam(name = "owner_role", required = false) SmsContactOwnerRole ownerRole,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return smsContactService
        .getByCriteria(
            contactGroupId,
            smsContactMapper.toDomain(ownerRole),
            PageRequest.of(page.getValue() - 1, pageSize.getValue()))
        .stream()
        .map(smsContactMapper::toRest)
        .toList();
  }

  @GetMapping("/sms-contacts/{id}")
  public SmsContact getSmsContactById(@PathVariable("id") String id) {
    return smsContactMapper.toRest(smsContactService.getById(id));
  }

  @DeleteMapping("/sms-contacts/{id}")
  public SmsContact deleteSmsContact(@PathVariable("id") String id) {
    return smsContactMapper.toRest(smsContactService.delete(id));
  }

  @PostMapping("/sms-contacts/{id}/messages")
  @ResponseStatus(ACCEPTED)
  public SmsCampaignLaunched sendSmsMessageToContact(
      @PathVariable("id") String id,
      @RequestBody SendSmsToContact toSend,
      @AuthenticationPrincipal Principal principal) {
    var result =
        smsCampaignService.createCampaign(
            principal.getUser(),
            toSend.getMessage(),
            null,
            List.of(id),
            null,
            null,
            null,
            toSend.getSendAt());
    return smsCampaignMapper.toLaunched(result.campaign(), result.rejectedRows());
  }
}
