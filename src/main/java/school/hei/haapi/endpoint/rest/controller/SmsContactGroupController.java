package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.SmsContactGroupMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateSmsContactGroup;
import school.hei.haapi.endpoint.rest.model.SmsContactGroup;
import school.hei.haapi.endpoint.rest.model.SmsContactGroupDetail;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.sms.SmsContactGroupService;

@RestController
@RequiredArgsConstructor
public class SmsContactGroupController {
  private final SmsContactGroupService smsContactGroupService;
  private final SmsContactGroupMapper smsContactGroupMapper;

  @GetMapping("/sms-contact-groups")
  public List<SmsContactGroup> getSmsContactGroups(
      @AuthenticationPrincipal Principal principal,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return smsContactGroupService
        .getByOwner(principal.getUser(), PageRequest.of(page.getValue() - 1, pageSize.getValue()))
        .stream()
        .map(smsContactGroupMapper::toRest)
        .toList();
  }

  @PostMapping("/sms-contact-groups")
  public SmsContactGroup createSmsContactGroup(
      @RequestBody CrupdateSmsContactGroup toCreate, @AuthenticationPrincipal Principal principal) {
    return smsContactGroupMapper.toRest(
        smsContactGroupService.create(
            principal.getUser(), toCreate.getName(), toCreate.getContactIds()));
  }

  @GetMapping("/sms-contact-groups/{id}")
  public SmsContactGroupDetail getSmsContactGroupById(@PathVariable("id") String id) {
    return smsContactGroupMapper.toRestDetail(smsContactGroupService.getById(id));
  }

  @PutMapping("/sms-contact-groups/{id}")
  public SmsContactGroup updateSmsContactGroup(
      @PathVariable("id") String id, @RequestBody CrupdateSmsContactGroup toUpdate) {
    return smsContactGroupMapper.toRest(
        smsContactGroupService.update(id, toUpdate.getName(), toUpdate.getContactIds()));
  }

  @DeleteMapping("/sms-contact-groups/{id}")
  public SmsContactGroup deleteSmsContactGroup(@PathVariable("id") String id) {
    return smsContactGroupMapper.toRest(smsContactGroupService.delete(id));
  }
}
