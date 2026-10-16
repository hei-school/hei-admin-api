package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.SmsDeliveryStatusPollTriggered;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignBatch;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.model.SmsLog;
import school.hei.haapi.model.SmsMessageStatus;
import school.hei.haapi.repository.SmsCampaignRepository;
import school.hei.haapi.repository.SmsLogRepository;
import school.hei.haapi.service.befiana.BefianaBatchMessagesPage;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.befiana.BefianaException;
import school.hei.haapi.service.befiana.BefianaMessageResponse;
import school.hei.haapi.service.event.SmsDeliveryStatusPollTriggeredService;

class SmsDeliveryStatusPollTriggeredServiceTest {
  private static final List<SmsMessageStatus> NON_TERMINAL =
      List.of(
          SmsMessageStatus.SCHEDULED,
          SmsMessageStatus.PROCESSING,
          SmsMessageStatus.ACCEPTED,
          SmsMessageStatus.UNKNOWN);

  private final SmsLogRepository smsLogRepositoryMock = mock();
  private final SmsCampaignRepository smsCampaignRepositoryMock = mock();
  private final BefianaClient befianaClientMock = mock();

  private final SmsDeliveryStatusPollTriggeredService subject =
      new SmsDeliveryStatusPollTriggeredService(
          smsLogRepositoryMock, smsCampaignRepositoryMock, befianaClientMock);

  private SmsLog unitaryPendingLog(SmsCampaign campaign, String befianaMessageId) {
    return SmsLog.builder()
        .id(befianaMessageId)
        .campaign(campaign)
        .phoneNumber("321111111")
        .status(SmsMessageStatus.PROCESSING)
        .befianaMessageId(befianaMessageId)
        .build();
  }

  private SmsLog batchedPendingLog(
      SmsCampaign campaign, SmsCampaignBatch batch, String phoneNumber) {
    return SmsLog.builder()
        .id(phoneNumber)
        .campaign(campaign)
        .campaignBatch(batch)
        .phoneNumber(phoneNumber)
        .status(SmsMessageStatus.SCHEDULED)
        .build();
  }

  private BefianaMessageResponse message(String id, String to, String status) {
    var response = new BefianaMessageResponse();
    response.setId(id);
    response.setTo(to);
    response.setStatus(status);
    return response;
  }

  private BefianaBatchMessagesPage page(String nextCursor, BefianaMessageResponse... results) {
    var page = new BefianaBatchMessagesPage();
    page.setResults(List.of(results));
    page.setNextCursor(nextCursor);
    return page;
  }

  @Test
  void no_pending_logs_does_nothing() {
    when(smsLogRepositoryMock.findAllByStatusIn(NON_TERMINAL)).thenReturn(List.of());

    subject.accept(new SmsDeliveryStatusPollTriggered());

    verify(befianaClientMock, never()).getMessage(any());
    verify(befianaClientMock, never()).getBatchMessages(any(), any());
    verify(smsCampaignRepositoryMock, never()).save(any());
  }

  @Test
  void a_unitary_log_is_refreshed_from_get_message() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var log = unitaryPendingLog(campaign, "msg-1");
    when(smsLogRepositoryMock.findAllByStatusIn(NON_TERMINAL)).thenReturn(List.of(log));
    when(befianaClientMock.getMessage("msg-1"))
        .thenReturn(message("msg-1", "321111111", "delivered"));
    when(smsLogRepositoryMock.countByCampaign_IdAndStatusIn(eq("campaign1"), anyList()))
        .thenReturn(0L);
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(1L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(SmsMessageStatus.DELIVERED, log.getStatus());
    verify(smsLogRepositoryMock).save(log);
  }

  @Test
  void a_befiana_error_for_one_unitary_log_does_not_stop_the_sweep() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var failing = unitaryPendingLog(campaign, "msg-fail");
    var ok = unitaryPendingLog(campaign, "msg-ok");
    when(smsLogRepositoryMock.findAllByStatusIn(NON_TERMINAL)).thenReturn(List.of(failing, ok));
    when(befianaClientMock.getMessage("msg-fail"))
        .thenThrow(new BefianaException("boom", null, null));
    when(befianaClientMock.getMessage("msg-ok"))
        .thenReturn(message("msg-ok", "321111111", "delivered"));
    when(smsLogRepositoryMock.countByCampaign_IdAndStatusIn(eq("campaign1"), anyList()))
        .thenReturn(0L);
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(1L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(SmsMessageStatus.PROCESSING, failing.getStatus());
    assertEquals(SmsMessageStatus.DELIVERED, ok.getStatus());
  }

  @Test
  void a_batched_log_is_matched_back_by_phone_number_across_a_single_page() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var batch =
        SmsCampaignBatch.builder().id("sb1").campaign(campaign).befianaBatchId("batch-1").build();
    var log1 = batchedPendingLog(campaign, batch, "321111111");
    var log2 = batchedPendingLog(campaign, batch, "321111112");
    when(smsLogRepositoryMock.findAllByStatusIn(NON_TERMINAL)).thenReturn(List.of(log1, log2));
    when(befianaClientMock.getBatchMessages("batch-1", null))
        .thenReturn(
            page(
                null,
                message("m1", "321111111", "delivered"),
                message("m2", "321111112", "failed")));
    when(smsLogRepositoryMock.countByCampaign_IdAndStatusIn(eq("campaign1"), anyList()))
        .thenReturn(0L);
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(1L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(SmsMessageStatus.DELIVERED, log1.getStatus());
    assertEquals("m1", log1.getBefianaMessageId());
    assertEquals(SmsMessageStatus.FAILED, log2.getStatus());
    assertEquals("m2", log2.getBefianaMessageId());
  }

  @Test
  void a_batch_poll_follows_the_cursor_across_several_pages() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var batch =
        SmsCampaignBatch.builder().id("sb1").campaign(campaign).befianaBatchId("batch-1").build();
    var log1 = batchedPendingLog(campaign, batch, "321111111");
    var log2 = batchedPendingLog(campaign, batch, "321111112");
    when(smsLogRepositoryMock.findAllByStatusIn(NON_TERMINAL)).thenReturn(List.of(log1, log2));
    when(befianaClientMock.getBatchMessages("batch-1", null))
        .thenReturn(page("cursor-2", message("m1", "321111111", "accepted")));
    when(befianaClientMock.getBatchMessages("batch-1", "cursor-2"))
        .thenReturn(page(null, message("m2", "321111112", "delivered")));
    when(smsLogRepositoryMock.countByCampaign_IdAndStatusIn(eq("campaign1"), anyList()))
        .thenReturn(0L);
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(1L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(SmsMessageStatus.ACCEPTED, log1.getStatus());
    assertEquals(SmsMessageStatus.DELIVERED, log2.getStatus());
    verify(befianaClientMock).getBatchMessages("batch-1", null);
    verify(befianaClientMock).getBatchMessages("batch-1", "cursor-2");
  }

  @Test
  void a_batch_page_result_for_an_unknown_phone_number_is_ignored() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var batch =
        SmsCampaignBatch.builder().id("sb1").campaign(campaign).befianaBatchId("batch-1").build();
    var log1 = batchedPendingLog(campaign, batch, "321111111");
    when(smsLogRepositoryMock.findAllByStatusIn(NON_TERMINAL)).thenReturn(List.of(log1));
    when(befianaClientMock.getBatchMessages("batch-1", null))
        .thenReturn(
            page(
                null,
                message("m1", "321111111", "delivered"),
                message("m2", "unrelated-number", "delivered")));
    when(smsLogRepositoryMock.countByCampaign_IdAndStatusIn(eq("campaign1"), anyList()))
        .thenReturn(0L);
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(1L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(SmsMessageStatus.DELIVERED, log1.getStatus());
  }

  @Test
  void a_befiana_error_for_one_batch_does_not_stop_polling_another_batch() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var failingBatch =
        SmsCampaignBatch.builder()
            .id("sb-fail")
            .campaign(campaign)
            .befianaBatchId("batch-fail")
            .build();
    var okBatch =
        SmsCampaignBatch.builder()
            .id("sb-ok")
            .campaign(campaign)
            .befianaBatchId("batch-ok")
            .build();
    var failingLog = batchedPendingLog(campaign, failingBatch, "321111111");
    var okLog = batchedPendingLog(campaign, okBatch, "321111112");
    when(smsLogRepositoryMock.findAllByStatusIn(NON_TERMINAL))
        .thenReturn(List.of(failingLog, okLog));
    when(befianaClientMock.getBatchMessages("batch-fail", null))
        .thenThrow(new BefianaException("boom", null, null));
    when(befianaClientMock.getBatchMessages("batch-ok", null))
        .thenReturn(page(null, message("m2", "321111112", "delivered")));
    when(smsLogRepositoryMock.countByCampaign_IdAndStatusIn(eq("campaign1"), anyList()))
        .thenReturn(0L);
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(1L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(SmsMessageStatus.SCHEDULED, failingLog.getStatus());
    assertEquals(SmsMessageStatus.DELIVERED, okLog.getStatus());
  }

  @Test
  void delivery_time_is_stored_when_befiana_returns_one() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var log = unitaryPendingLog(campaign, "msg-1");
    var response = message("msg-1", "321111111", "delivered");
    response.setDeliveryTime(Instant.parse("2030-01-15T06:05:00Z"));
    when(smsLogRepositoryMock.findAllByStatusIn(NON_TERMINAL)).thenReturn(List.of(log));
    when(befianaClientMock.getMessage("msg-1")).thenReturn(response);
    when(smsLogRepositoryMock.countByCampaign_IdAndStatusIn(eq("campaign1"), anyList()))
        .thenReturn(0L);
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(1L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(Instant.parse("2030-01-15T06:05:00Z"), log.getDeliveredDatetime());
  }

  @Test
  void a_log_still_without_a_befiana_message_id_is_skipped() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var log =
        SmsLog.builder()
            .id("l1")
            .campaign(campaign)
            .phoneNumber("321111111")
            .status(SmsMessageStatus.UNKNOWN)
            .build();
    when(smsLogRepositoryMock.findAllByStatusIn(NON_TERMINAL)).thenReturn(List.of(log));

    subject.accept(new SmsDeliveryStatusPollTriggered());

    verify(befianaClientMock, never()).getMessage(any());
    assertNull(log.getBefianaMessageId());
  }

  @Test
  void campaign_is_resolved_delivered_once_nothing_is_left_non_terminal() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var log = unitaryPendingLog(campaign, "msg-1");
    when(smsLogRepositoryMock.findAllByStatusIn(NON_TERMINAL)).thenReturn(List.of(log));
    when(befianaClientMock.getMessage("msg-1"))
        .thenReturn(message("msg-1", "321111111", "delivered"));
    when(smsLogRepositoryMock.countByCampaign_IdAndStatusIn(eq("campaign1"), anyList()))
        .thenReturn(0L);
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(1L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(1, campaign.getDeliveredCount());
    assertEquals(SmsCampaignStatus.DELIVERED, campaign.getStatus());
    verify(smsCampaignRepositoryMock, times(1)).save(campaign);
  }

  @Test
  void campaign_is_resolved_failed_when_nothing_was_ever_delivered() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var log = unitaryPendingLog(campaign, "msg-1");
    when(smsLogRepositoryMock.findAllByStatusIn(NON_TERMINAL)).thenReturn(List.of(log));
    when(befianaClientMock.getMessage("msg-1"))
        .thenReturn(message("msg-1", "321111111", "undeliverable"));
    when(smsLogRepositoryMock.countByCampaign_IdAndStatusIn(eq("campaign1"), anyList()))
        .thenReturn(0L);
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(0L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(SmsCampaignStatus.FAILED, campaign.getStatus());
  }

  @Test
  void campaign_is_not_resolved_while_other_recipients_are_still_non_terminal() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var log = unitaryPendingLog(campaign, "msg-1");
    when(smsLogRepositoryMock.findAllByStatusIn(NON_TERMINAL)).thenReturn(List.of(log));
    when(befianaClientMock.getMessage("msg-1"))
        .thenReturn(message("msg-1", "321111111", "delivered"));
    when(smsLogRepositoryMock.countByCampaign_IdAndStatusIn(eq("campaign1"), anyList()))
        .thenReturn(1L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    verify(smsCampaignRepositoryMock, never()).save(any());
  }
}
