package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.SmsCampaignDispatchRequested;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.model.SmsLog;
import school.hei.haapi.model.SmsMessageStatus;
import school.hei.haapi.repository.SmsCampaignRepository;
import school.hei.haapi.repository.SmsLogRepository;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.befiana.BefianaException;
import school.hei.haapi.service.befiana.BefianaSendBulkResponse;
import school.hei.haapi.service.befiana.BefianaSendResponse;
import school.hei.haapi.service.event.SmsCampaignDispatchRequestedService;
import school.hei.haapi.service.sms.NotificationService;
import school.hei.haapi.service.sms.SmsSegmentCounter;

class SmsCampaignDispatchRequestedServiceTest {
  private final SmsCampaignRepository smsCampaignRepositoryMock = mock();
  private final SmsLogRepository smsLogRepositoryMock = mock();
  private final BefianaClient befianaClientMock = mock();
  private final NotificationService notificationServiceMock = mock();

  private final SmsCampaignDispatchRequestedService subject =
      new SmsCampaignDispatchRequestedService(
          smsCampaignRepositoryMock,
          smsLogRepositoryMock,
          befianaClientMock,
          new SmsSegmentCounter(),
          notificationServiceMock);

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
  void exactly_one_shared_recipient_uses_unitary_send() {
    var campaign = campaign("Hi", 1);
    var log = sharedLog(campaign, "321111111");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log));
    when(befianaClientMock.getBalance()).thenReturn(10);
    var response = new BefianaSendResponse();
    response.setCallbackData("cb1");
    when(befianaClientMock.send("321111111", "Hi", null)).thenReturn(response);

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(befianaClientMock, times(1)).send("321111111", "Hi", null);
    verify(befianaClientMock, never()).sendBulk(anyList(), anyString(), any());
    assertEquals(SmsMessageStatus.PENDING, log.getStatus());
    assertEquals("cb1", log.getCallbackData());
    assertEquals(SmsCampaignStatus.DELIVERED, campaign.getStatus());
  }

  @Test
  void two_or_more_shared_recipients_use_bulk_send_and_stay_untracked() {
    var campaign = campaign("Hi", 2);
    var log1 = sharedLog(campaign, "321111111");
    var log2 = sharedLog(campaign, "321111112");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log1, log2));
    when(befianaClientMock.getBalance()).thenReturn(10);
    var response = new BefianaSendBulkResponse();
    response.setSmsSegmentsEach(1);
    response.setBalanceDebited(2);
    when(befianaClientMock.sendBulk(List.of("321111111", "321111112"), "Hi", null))
        .thenReturn(response);

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(befianaClientMock, never()).send(anyString(), anyString(), any());
    verify(befianaClientMock, times(1)).sendBulk(List.of("321111111", "321111112"), "Hi", null);
    // BEFIANA gives no callbackData for bulk -> status is never set, delivery stays unknowable.
    assertNull(log1.getStatus());
    assertNull(log2.getStatus());
    assertEquals(1, campaign.getSmsSegmentsEach());
    assertEquals(2, campaign.getCreditsDebited());
    assertEquals(SmsCampaignStatus.DELIVERED, campaign.getStatus());
  }

  @Test
  void personalized_recipients_are_sent_individually_with_their_own_message() {
    var campaign = campaign("shared message unused here", 2);
    var log1 = personalizedLog(campaign, "321111111", "Bonjour A");
    var log2 = personalizedLog(campaign, "321111112", "Bonjour B");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log1, log2));
    when(befianaClientMock.getBalance()).thenReturn(10);
    when(befianaClientMock.send(anyString(), anyString(), isNull()))
        .thenReturn(new BefianaSendResponse());

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(befianaClientMock).send("321111111", "Bonjour A", null);
    verify(befianaClientMock).send("321111112", "Bonjour B", null);
    verify(befianaClientMock, never()).sendBulk(anyList(), anyString(), any());
  }

  @Test
  void
      a_rejected_bulk_chunk_marks_its_recipients_failed_and_fails_the_campaign_if_nothing_else_went_through() {
    var campaign = campaign("Hi", 2);
    var log1 = sharedLog(campaign, "321111111");
    var log2 = sharedLog(campaign, "321111112");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log1, log2));
    when(befianaClientMock.getBalance()).thenReturn(10);
    when(befianaClientMock.sendBulk(anyList(), anyString(), any()))
        .thenThrow(new BefianaException("BEFIANA is down", 500, null));
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.FAILED))
        .thenReturn(2L);

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    assertEquals(SmsMessageStatus.FAILED, log1.getStatus());
    assertEquals(SmsMessageStatus.FAILED, log2.getStatus());
    assertEquals(SmsCampaignStatus.FAILED, campaign.getStatus());
    assertEquals(
        "BEFIANA a rejeté l'envoi pour tous les destinataires.", campaign.getFailureReason());
    verify(notificationServiceMock)
        .notifyAdminsOfCampaignOutcome(eq(campaign), anyString(), anyString());
  }

  @Test
  void
      balance_dropping_below_two_recipients_at_dispatch_time_falls_back_to_a_trackable_unitary_send() {
    // Both recipients cost 1 segment each; a balance of 1 at dispatch time can only cover one of
    // them -> the survivor becomes a lone shared recipient, which is sent trackably via /send/.
    var campaign = campaign("Hi", 2);
    var log1 = sharedLog(campaign, "321111111");
    var log2 = sharedLog(campaign, "321111112");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log1, log2));
    when(befianaClientMock.getBalance()).thenReturn(1);
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.FAILED))
        .thenReturn(0L);
    when(befianaClientMock.send(eq("321111111"), eq("Hi"), any()))
        .thenReturn(new BefianaSendResponse());

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(smsLogRepositoryMock).deleteAll(List.of(log2));
    assertEquals(1, campaign.getRecipientsRejectedForBalance());
    verify(befianaClientMock, times(1)).send(eq("321111111"), eq("Hi"), any());
    assertEquals(SmsCampaignStatus.DELIVERED, campaign.getStatus());
    verify(notificationServiceMock)
        .notifyAdminsOfCampaignOutcome(eq(campaign), anyString(), anyString());
  }

  @Test
  void zero_balance_at_dispatch_time_fails_the_whole_campaign() {
    var campaign = campaign("Hi", 2);
    var log1 = sharedLog(campaign, "321111111");
    var log2 = sharedLog(campaign, "321111112");
    when(smsCampaignRepositoryMock.findById("campaign1")).thenReturn(Optional.of(campaign));
    stubLogs(campaign, List.of(log1, log2));
    when(befianaClientMock.getBalance()).thenReturn(0);
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.FAILED))
        .thenReturn(0L);

    subject.accept(new SmsCampaignDispatchRequested("campaign1"));

    verify(befianaClientMock, never()).send(anyString(), anyString(), any());
    verify(befianaClientMock, never()).sendBulk(anyList(), anyString(), any());
    assertEquals(2, campaign.getRecipientsRejectedForBalance());
    assertEquals(SmsCampaignStatus.FAILED, campaign.getStatus());
    assertEquals(
        "Solde insuffisant au moment de l'envoi effectif de la campagne.",
        campaign.getFailureReason());
    verify(notificationServiceMock)
        .notifyAdminsOfCampaignOutcome(eq(campaign), anyString(), anyString());
  }
}
