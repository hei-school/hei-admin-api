package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.SmsCampaignDispatchRequested;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.model.SmsLog;
import school.hei.haapi.model.SmsMessageStatus;
import school.hei.haapi.repository.SmsCampaignBatchRepository;
import school.hei.haapi.repository.SmsCampaignRepository;
import school.hei.haapi.repository.SmsLogRepository;
import school.hei.haapi.service.befiana.BefianaBalanceResponse;
import school.hei.haapi.service.befiana.BefianaBatchResponse;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.befiana.BefianaException;
import school.hei.haapi.service.befiana.BefianaMessageResponse;
import school.hei.haapi.service.event.SmsCampaignDispatchRequestedService;
import school.hei.haapi.service.sms.SmsSegmentCounter;

class SmsCampaignDispatchRequestedServiceTest {
  private final SmsCampaignRepository smsCampaignRepositoryMock = mock();
  private final SmsLogRepository smsLogRepositoryMock = mock();
  private final SmsCampaignBatchRepository smsCampaignBatchRepositoryMock = mock();
  private final BefianaClient befianaClientMock = mock();

  private final SmsCampaignDispatchRequestedService subject =
      new SmsCampaignDispatchRequestedService(
          smsCampaignRepositoryMock,
          smsLogRepositoryMock,
          smsCampaignBatchRepositoryMock,
          befianaClientMock,
          new SmsSegmentCounter());

  SmsCampaignDispatchRequestedServiceTest() {
    when(smsCampaignBatchRepositoryMock.save(any())).thenAnswer(inv -> inv.getArgument(0));
  }

  private SmsCampaign campaign(String message, int recipientCount) {
    return SmsCampaign.builder()
        .id("campaign1")
        .message(message)
        .status(SmsCampaignStatus.CREATED)
        .recipientCount(recipientCount)
        .recipientsRejectedForBalance(0)
        .build();
  }

  private SmsLog sharedLog(SmsCampaign campaign, String phoneNumber) {
    return SmsLog.builder().id(phoneNumber).campaign(campaign).phoneNumber(phoneNumber).build();
  }

  private SmsLog personalizedLog(SmsCampaign campaign, String phoneNumber, String message) {
    return SmsLog.builder()
        .id(phoneNumber)
        .campaign(campaign)
        .phoneNumber(phoneNumber)
        .personalizedMessage(message)
        .build();
  }

  private void stubLogs(SmsCampaign campaign, List<SmsLog> logs) {
    when(smsLogRepositoryMock.findAllByCampaign_IdOrderBySentDatetimeDesc(campaign.getId()))
        .thenReturn(new ArrayList<>(logs));
  }

  private void stubBalance(int available) {
    var response = new BefianaBalanceResponse();
    response.setAvailable(available);
    when(befianaClientMock.getBalance()).thenReturn(response);
  }

  private BefianaMessageResponse messageResponse(String id, String status) {
    var response = new BefianaMessageResponse();
    response.setId(id);
    response.setStatus(status);
    return response;
  }

  @Test
  void unknown_campaign_is_dropped_silently() {
    when(smsCampaignRepositoryMock.findById("missing")).thenReturn(Optional.empty());

    subject.accept(new SmsCampaignDispatchRequested("missing"));

    verify(smsCampaignRepositoryMock, never()).save(any());
    verify(befianaClientMock, never()).getBalance();
  }

  @Test
  void an_already_delivered_campaign_is_not_reprocessed() {
    var campaign = campaign("Hi", 1);
    campaign.setStatus(SmsCampaignStatus.DELIVERED);
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(befianaClientMock, never()).getBalance();
  }

  @Test
  void exactly_one_shared_recipient_uses_a_unitary_messages_call() {
    var campaign = campaign("Hi", 1);
    var log = sharedLog(campaign, "321111111");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log));
    stubBalance(10);
    when(befianaClientMock.sendSingle("321111111", "Hi", log.getId()))
        .thenReturn(messageResponse("msg-1", "accepted"));

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(befianaClientMock, times(1)).sendSingle("321111111", "Hi", log.getId());
    verify(befianaClientMock, never()).createBatch(anyList(), anyString(), any(), anyString());
    assertEquals(SmsMessageStatus.ACCEPTED, log.getStatus());
    assertEquals("msg-1", log.getBefianaMessageId());
    assertNotNull(log.getSentDatetime());
    assertEquals(SmsCampaignStatus.PENDING, campaign.getStatus());
  }

  @Test
  void two_or_more_shared_recipients_use_a_message_batch_and_stay_pending() {
    var campaign = campaign("Hi", 2);
    var log1 = sharedLog(campaign, "321111111");
    var log2 = sharedLog(campaign, "321111112");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log1, log2));
    stubBalance(10);
    var response = new BefianaBatchResponse();
    response.setId("batch-1");
    response.setCreditsReserved(2);
    when(befianaClientMock.createBatch(
            eq(List.of("321111111", "321111112")), eq("Hi"), any(), anyString()))
        .thenReturn(response);

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(befianaClientMock, never()).sendSingle(anyString(), anyString(), anyString());
    verify(befianaClientMock, times(1))
        .createBatch(eq(List.of("321111111", "321111112")), eq("Hi"), any(), anyString());
    assertEquals(SmsMessageStatus.SCHEDULED, log1.getStatus());
    assertEquals(SmsMessageStatus.SCHEDULED, log2.getStatus());
    assertEquals("batch-1", log1.getCampaignBatch().getBefianaBatchId());
    assertEquals(log1.getCampaignBatch(), log2.getCampaignBatch());
    assertEquals(2, campaign.getCreditsDebited());
    assertEquals(SmsCampaignStatus.PENDING, campaign.getStatus());
  }

  @Test
  void a_campaign_with_an_explicit_send_at_forwards_it_to_the_batch_call() {
    var campaign = campaign("Hi", 2);
    var sendAt = Instant.parse("2030-01-15T06:00:00Z");
    campaign.setSendAt(sendAt);
    var log1 = sharedLog(campaign, "321111111");
    var log2 = sharedLog(campaign, "321111112");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log1, log2));
    stubBalance(10);
    when(befianaClientMock.createBatch(anyList(), anyString(), any(), anyString()))
        .thenReturn(new BefianaBatchResponse());

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(befianaClientMock).createBatch(anyList(), anyString(), eq(sendAt), anyString());
  }

  @Test
  void a_campaign_with_more_than_1000_shared_recipients_is_split_into_several_batches() {
    var campaign = campaign("Hi", 1200);
    var logs = new ArrayList<SmsLog>();
    for (var i = 0; i < 1200; i++) {
      logs.add(sharedLog(campaign, "32111" + i));
    }
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, logs);
    stubBalance(10_000);
    when(befianaClientMock.createBatch(anyList(), anyString(), any(), anyString()))
        .thenReturn(new BefianaBatchResponse());

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(befianaClientMock, times(2)).createBatch(anyList(), anyString(), any(), anyString());
  }

  @Test
  void personalized_recipients_are_sent_individually_via_messages() {
    var campaign = campaign("shared message unused here", 2);
    var log1 = personalizedLog(campaign, "321111111", "Bonjour A");
    var log2 = personalizedLog(campaign, "321111112", "Bonjour B");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log1, log2));
    stubBalance(10);
    when(befianaClientMock.sendSingle(anyString(), anyString(), anyString()))
        .thenReturn(messageResponse("msg-x", "processing"));

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(befianaClientMock).sendSingle("321111111", "Bonjour A", log1.getId());
    verify(befianaClientMock).sendSingle("321111112", "Bonjour B", log2.getId());
    verify(befianaClientMock, never()).createBatch(anyList(), anyString(), any(), anyString());
  }

  @Test
  void
      a_rejected_batch_marks_its_recipients_failed_and_fails_the_campaign_if_nothing_else_went_through() {
    var campaign = campaign("Hi", 2);
    var log1 = sharedLog(campaign, "321111111");
    var log2 = sharedLog(campaign, "321111112");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log1, log2));
    stubBalance(10);
    when(befianaClientMock.createBatch(anyList(), anyString(), any(), anyString()))
        .thenThrow(
            new BefianaException(
                "BEFIANA call to /api/sms/v2/message-batches/ failed: HTTP 500 - boom", 500, null));
    when(smsLogRepositoryMock.countByCampaign_IdAndStatusIn(eq("campaign1"), anyList()))
        .thenReturn(2L);

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    assertEquals(SmsMessageStatus.FAILED, log1.getStatus());
    assertEquals(SmsMessageStatus.FAILED, log2.getStatus());
    assertEquals(
        "BEFIANA call to /api/sms/v2/message-batches/ failed: HTTP 500 - boom",
        log1.getFailureReason());
    assertEquals(SmsCampaignStatus.FAILED, campaign.getStatus());
    assertEquals(
        "BEFIANA a rejeté l'envoi pour tous les destinataires.", campaign.getFailureReason());
  }

  @Test
  void balance_dropping_below_two_recipients_at_dispatch_time_falls_back_to_a_unitary_send() {
    var campaign = campaign("Hi", 2);
    var affordableRecipientLog = sharedLog(campaign, "321111111");
    var unaffordableRecipientLog = sharedLog(campaign, "321111112");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(affordableRecipientLog, unaffordableRecipientLog));
    stubBalance(1);
    when(befianaClientMock.sendSingle("321111111", "Hi", affordableRecipientLog.getId()))
        .thenReturn(messageResponse("msg-1", "accepted"));

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(smsLogRepositoryMock).deleteAll(List.of(unaffordableRecipientLog));
    assertEquals(1, campaign.getRecipientsRejectedForBalance());
    verify(befianaClientMock, times(1))
        .sendSingle("321111111", "Hi", affordableRecipientLog.getId());
    assertEquals(SmsCampaignStatus.PENDING, campaign.getStatus());
  }

  @Test
  void a_failed_unitary_send_marks_the_log_failed_with_the_befiana_reason() {
    var campaign = campaign("shared message unused here", 2);
    var log1 = personalizedLog(campaign, "321111111", "Bonjour A");
    var log2 = personalizedLog(campaign, "321111112", "Bonjour B");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log1, log2));
    stubBalance(10);
    when(befianaClientMock.sendSingle("321111111", "Bonjour A", log1.getId()))
        .thenReturn(messageResponse("msg-1", "processing"));
    when(befianaClientMock.sendSingle("321111112", "Bonjour B", log2.getId()))
        .thenThrow(
            new BefianaException(
                "BEFIANA call to /api/sms/v2/messages/ failed: HTTP 400 - numéro invalide",
                400,
                null));
    when(smsLogRepositoryMock.countByCampaign_IdAndStatusIn(eq("campaign1"), anyList()))
        .thenReturn(1L);

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    assertEquals(SmsMessageStatus.PROCESSING, log1.getStatus());
    assertNull(log1.getFailureReason());
    assertEquals(SmsMessageStatus.FAILED, log2.getStatus());
    assertEquals(
        "BEFIANA call to /api/sms/v2/messages/ failed: HTTP 400 - numéro invalide",
        log2.getFailureReason());
    assertEquals(SmsCampaignStatus.PENDING, campaign.getStatus());
    assertEquals(1, campaign.getFailedCount());
  }

  @Test
  void zero_balance_at_dispatch_time_fails_the_whole_campaign() {
    var campaign = campaign("Hi", 2);
    var log1 = sharedLog(campaign, "321111111");
    var log2 = sharedLog(campaign, "321111112");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log1, log2));
    stubBalance(0);

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(befianaClientMock, never()).sendSingle(anyString(), anyString(), anyString());
    verify(befianaClientMock, never()).createBatch(anyList(), anyString(), any(), anyString());
    assertEquals(2, campaign.getRecipientsRejectedForBalance());
    assertEquals(SmsCampaignStatus.FAILED, campaign.getStatus());
    assertEquals(
        "Solde insuffisant au moment de l'envoi effectif de la campagne.",
        campaign.getFailureReason());
  }
}
