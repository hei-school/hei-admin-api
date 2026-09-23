package school.hei.haapi.endpoint.rest.controller;

import static org.springframework.http.HttpStatus.ACCEPTED;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.SmsCampaignMapper;
import school.hei.haapi.endpoint.rest.mapper.SmsLogMapper;
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

  @PostMapping(value = "/sms-campaigns", consumes = "multipart/form-data")
  @ResponseStatus(ACCEPTED)
  public SmsCampaignLaunched createSmsCampaign(
      @RequestParam(required = false) String message,
      @RequestParam(required = false) List<String> contactGroupIds,
      @RequestParam(required = false) List<String> contactIds,
      @RequestParam(required = false) List<String> manualPhoneNumbers,
      @RequestPart(value = "file", required = false) MultipartFile file,
      @RequestParam(required = false) Instant sendAt,
      @AuthenticationPrincipal Principal principal) {
    var result =
        smsCampaignService.createCampaign(
            new SmsCampaignService.CreateSmsCampaignCommand(
                principal.getUser(),
                message,
                contactGroupIds,
                contactIds,
                manualPhoneNumbers,
                file == null || file.isEmpty() ? null : fileConverter.apply(file),
                file == null || file.isEmpty() ? null : file.getOriginalFilename(),
                sendAt));
    return smsCampaignMapper.toLaunched(result.campaign(), result.rejectedRows());
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
