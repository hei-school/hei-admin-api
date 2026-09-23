package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import school.hei.haapi.endpoint.rest.controller.SmsCampaignController;
import school.hei.haapi.endpoint.rest.mapper.SmsCampaignMapper;
import school.hei.haapi.endpoint.rest.mapper.SmsLogMapper;
import school.hei.haapi.endpoint.rest.model.SmsCampaignLaunched;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.model.SmsLog;
import school.hei.haapi.model.SmsMessageStatus;
import school.hei.haapi.model.User;
import school.hei.haapi.service.MultipartFileConverter;
import school.hei.haapi.service.sms.SmsCampaignService;
import school.hei.haapi.service.sms.SmsCampaignService.CreateSmsCampaignCommand;
import school.hei.haapi.service.sms.SmsLogService;

class SmsCampaignControllerTest {
  private final SmsCampaignService smsCampaignServiceMock = mock();
  private final SmsCampaignMapper smsCampaignMapper = new SmsCampaignMapper();
  private final SmsLogService smsLogServiceMock = mock();
  private final SmsLogMapper smsLogMapper = new SmsLogMapper();
  private final MultipartFileConverter fileConverterMock = mock();

  private final SmsCampaignController subject =
      new SmsCampaignController(
          smsCampaignServiceMock,
          smsCampaignMapper,
          smsLogServiceMock,
          smsLogMapper,
          fileConverterMock);

  private final Principal principal = new Principal(User.builder().id("admin1").build(), "token");

  private SmsCampaign campaignResult() {
    return SmsCampaign.builder()
        .id("campaign1")
        .status(SmsCampaignStatus.CREATED)
        .recipientCount(1)
        .build();
  }

  @Test
  void creates_a_campaign_without_a_file() {
    var result = new SmsCampaignService.CreationResult(campaignResult(), List.of());
    when(smsCampaignServiceMock.createCampaign(any(CreateSmsCampaignCommand.class)))
        .thenReturn(result);

    SmsCampaignLaunched launched =
        subject.createSmsCampaign(
            "Hello", List.of("g1"), null, List.of("321111111"), null, null, principal);

    assertEquals("campaign1", launched.getCampaignId());
    verify(fileConverterMock, never()).apply(any());
  }

  @Test
  void creates_a_campaign_with_a_non_empty_file() {
    var result = new SmsCampaignService.CreationResult(campaignResult(), List.of());
    when(smsCampaignServiceMock.createCampaign(any(CreateSmsCampaignCommand.class)))
        .thenReturn(result);
    var multipartFile =
        new MockMultipartFile("file", "numbers.xlsx", "application/octet-stream", new byte[] {1});
    var convertedFile = mock(File.class);
    when(fileConverterMock.apply(multipartFile)).thenReturn(convertedFile);

    subject.createSmsCampaign(null, null, null, null, multipartFile, null, principal);

    verify(smsCampaignServiceMock)
        .createCampaign(
            eq(
                new CreateSmsCampaignCommand(
                    principal.getUser(),
                    null,
                    null,
                    null,
                    null,
                    convertedFile,
                    "numbers.xlsx",
                    null)));
  }

  @Test
  void an_empty_multipart_file_is_treated_as_no_file_at_all() {
    var result = new SmsCampaignService.CreationResult(campaignResult(), List.of());
    when(smsCampaignServiceMock.createCampaign(any(CreateSmsCampaignCommand.class)))
        .thenReturn(result);
    var emptyFile =
        new MockMultipartFile("file", "empty.xlsx", "application/octet-stream", new byte[0]);

    subject.createSmsCampaign(null, null, null, null, emptyFile, null, principal);

    verify(fileConverterMock, never()).apply(any());
    verify(smsCampaignServiceMock)
        .createCampaign(
            eq(
                new CreateSmsCampaignCommand(
                    principal.getUser(), null, null, null, null, null, null, null)));
  }

  @Test
  void lists_campaigns_converting_the_status_filter_to_the_domain_enum() {
    when(smsCampaignServiceMock.getByCriteria(eq(SmsCampaignStatus.DELIVERED), any()))
        .thenReturn(List.of(campaignResult()));

    var page =
        subject.getSmsCampaigns(
            school.hei.haapi.endpoint.rest.model.SmsCampaignStatus.DELIVERED,
            new PageFromOne(1),
            new BoundedPageSize(10));

    assertEquals(1, page.size());
    assertEquals("campaign1", page.get(0).getId());
  }

  @Test
  void lists_campaigns_with_no_status_filter() {
    when(smsCampaignServiceMock.getByCriteria(isNull(), any())).thenReturn(List.of());

    var page = subject.getSmsCampaigns(null, new PageFromOne(1), new BoundedPageSize(10));

    assertEquals(0, page.size());
  }

  @Test
  void gets_a_single_campaign_by_id() {
    when(smsCampaignServiceMock.getById("campaign1")).thenReturn(campaignResult());

    assertEquals("campaign1", subject.getSmsCampaignById("campaign1").getId());
  }

  @Test
  void gets_campaign_logs_paginated() {
    var campaign = campaignResult();
    var logs =
        List.of(
            SmsLog.builder().id("l1").campaign(campaign).phoneNumber("321111111").build(),
            SmsLog.builder().id("l2").campaign(campaign).phoneNumber("321111112").build(),
            SmsLog.builder().id("l3").campaign(campaign).phoneNumber("321111113").build());
    when(smsLogServiceMock.getByCampaignId("campaign1", null)).thenReturn(logs);

    var firstPage =
        subject.getSmsCampaignLogs("campaign1", null, new PageFromOne(1), new BoundedPageSize(2));
    var secondPage =
        subject.getSmsCampaignLogs("campaign1", null, new PageFromOne(2), new BoundedPageSize(2));

    assertEquals(2, firstPage.size());
    assertEquals("l1", firstPage.get(0).getId());
    assertEquals(1, secondPage.size());
    assertEquals("l3", secondPage.get(0).getId());
  }

  @Test
  void gets_campaign_logs_filtered_by_status() {
    var campaign = campaignResult();
    var log =
        SmsLog.builder()
            .id("l1")
            .campaign(campaign)
            .phoneNumber("321111111")
            .status(SmsMessageStatus.DELIVERED)
            .build();
    when(smsLogServiceMock.getByCampaignId("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(List.of(log));

    var page =
        subject.getSmsCampaignLogs(
            "campaign1",
            school.hei.haapi.endpoint.rest.model.SmsMessageStatus.DELIVERED,
            new PageFromOne(1),
            new BoundedPageSize(10));

    assertEquals(1, page.size());
  }

  @Test
  void a_page_past_the_end_returns_an_empty_list() {
    when(smsLogServiceMock.getByCampaignId("campaign1", null)).thenReturn(List.of());

    var page =
        subject.getSmsCampaignLogs("campaign1", null, new PageFromOne(3), new BoundedPageSize(10));

    assertEquals(0, page.size());
  }
}
