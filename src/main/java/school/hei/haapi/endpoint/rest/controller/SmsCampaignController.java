package school.hei.haapi.endpoint.rest.controller;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.SmsCampaignMapper;
import school.hei.haapi.endpoint.rest.mapper.SmsLogMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateSmsCampaignByContacts;
import school.hei.haapi.endpoint.rest.model.CrupdateSmsCampaignByGroups;
import school.hei.haapi.endpoint.rest.model.CrupdateSmsCampaignByManualNumbers;
import school.hei.haapi.endpoint.rest.model.SmsCampaign;
import school.hei.haapi.endpoint.rest.model.SmsCampaignLaunched;
import school.hei.haapi.endpoint.rest.model.SmsCampaignStatus;
import school.hei.haapi.endpoint.rest.model.SmsLog;
import school.hei.haapi.endpoint.rest.model.SmsMessageStatus;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.MultipartFileConverter;
import school.hei.haapi.service.sms.SmsCampaignService;
import school.hei.haapi.service.sms.SmsLogService;

@RestController
@RequiredArgsConstructor
public class SmsCampaignController {
  private final SmsCampaignService smsCampaignService;
  private final SmsCampaignMapper smsCampaignMapper;
  private final SmsLogService smsLogService;
  private final SmsLogMapper smsLogMapper;
  private final MultipartFileConverter fileConverter;

  @PostMapping("/sms-campaigns/by-contacts")
  @ResponseStatus(ACCEPTED)
  public SmsCampaignLaunched createSmsCampaignByContacts(
      @RequestBody CrupdateSmsCampaignByContacts crupdate,
      @AuthenticationPrincipal Principal principal) {
    var result =
        smsCampaignService.createCampaign(
            new SmsCampaignService.CreateSmsCampaignCommand(
                principal.getUser(),
                crupdate.getMessage(),
                null,
                crupdate.getContactIds(),
                null,
                null,
                null));
    return smsCampaignMapper.toLaunched(result.campaign());
  }

  @PostMapping("/sms-campaigns/by-manual-numbers")
  @ResponseStatus(ACCEPTED)
  public SmsCampaignLaunched createSmsCampaignByManualNumbers(
      @RequestBody CrupdateSmsCampaignByManualNumbers crupdate,
      @AuthenticationPrincipal Principal principal) {
    var result =
        smsCampaignService.createCampaign(
            new SmsCampaignService.CreateSmsCampaignCommand(
                principal.getUser(),
                crupdate.getMessage(),
                null,
                null,
                crupdate.getManualPhoneNumbers(),
                null,
                null));
    return smsCampaignMapper.toLaunched(result.campaign());
  }

  @PostMapping("/sms-campaigns/by-groups")
  @ResponseStatus(ACCEPTED)
  public SmsCampaignLaunched createSmsCampaignByGroups(
      @RequestBody CrupdateSmsCampaignByGroups crupdate,
      @AuthenticationPrincipal Principal principal) {
    var result =
        smsCampaignService.createCampaign(
            new SmsCampaignService.CreateSmsCampaignCommand(
                principal.getUser(),
                crupdate.getMessage(),
                crupdate.getContactGroupIds(),
                null,
                null,
                null,
                null));
    return smsCampaignMapper.toLaunched(result.campaign());
  }

  @PostMapping(value = "/sms-campaigns/by-file", consumes = MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(ACCEPTED)
  public SmsCampaignLaunched createSmsCampaignByFile(
      @RequestParam(required = false) String message,
      @RequestPart("file") MultipartFile file,
      @AuthenticationPrincipal Principal principal) {
    var result =
        smsCampaignService.createCampaign(
            new SmsCampaignService.CreateSmsCampaignCommand(
                principal.getUser(),
                message,
                null,
                null,
                null,
                fileConverter.apply(file),
                file.getOriginalFilename()));
    return smsCampaignMapper.toLaunched(result.campaign());
  }

  @GetMapping("/sms-campaigns")
  public List<SmsCampaign> getSmsCampaigns(
      @RequestParam(name = "status", required = false) SmsCampaignStatus status,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return smsCampaignService
        .getByCriteria(
            smsCampaignMapper.toDomain(status),
            PageRequest.of(page.getValue() - 1, pageSize.getValue()))
        .stream()
        .map(smsCampaignMapper::toRest)
        .toList();
  }

  @GetMapping("/sms-campaigns/{id}")
  public SmsCampaign getSmsCampaignById(@PathVariable("id") String id) {
    return smsCampaignMapper.toRest(smsCampaignService.getById(id));
  }

  @GetMapping("/sms-campaigns/{id}/logs")
  public List<SmsLog> getSmsCampaignLogs(
      @PathVariable("id") String id,
      @RequestParam(name = "status", required = false) SmsMessageStatus status,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    var all = smsLogService.getByCampaignId(id, smsLogMapper.toDomain(status));
    var fromIndex = Math.min((page.getValue() - 1) * pageSize.getValue(), all.size());
    var toIndex = Math.min(fromIndex + pageSize.getValue(), all.size());
    return all.subList(fromIndex, toIndex).stream().map(smsLogMapper::toRest).toList();
  }
}
