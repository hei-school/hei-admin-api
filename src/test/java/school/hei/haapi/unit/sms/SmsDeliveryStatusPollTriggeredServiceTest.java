package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.SmsDeliveryStatusPollTriggered;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsLog;
import school.hei.haapi.model.SmsMessageStatus;
import school.hei.haapi.repository.SmsCampaignRepository;
import school.hei.haapi.repository.SmsLogRepository;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.befiana.BefianaDeliveryStatusResponse;
import school.hei.haapi.service.befiana.BefianaException;
import school.hei.haapi.service.event.SmsDeliveryStatusPollTriggeredService;

class SmsDeliveryStatusPollTriggeredServiceTest {
  private final SmsLogRepository smsLogRepositoryMock = mock();
  private final SmsCampaignRepository smsCampaignRepositoryMock = mock();
  private final BefianaClient befianaClientMock = mock();

  private final SmsDeliveryStatusPollTriggeredService subject =
      new SmsDeliveryStatusPollTriggeredService(
          smsLogRepositoryMock, smsCampaignRepositoryMock, befianaClientMock);

  private SmsLog pendingLog(SmsCampaign campaign, String callbackData) {
    return SmsLog.builder()
        .id(callbackData)
        .campaign(campaign)
        .phoneNumber("321111111")
        .status(SmsMessageStatus.PENDING)
        .callbackData(callbackData)
        .build();
  }

  private BefianaDeliveryStatusResponse deliveryStatus(String status) {
    var response = new BefianaDeliveryStatusResponse();
    response.setDeliveryStatus(status);
    return response;
  }

  @Test
  void no_pending_logs_does_nothing() {
    when(smsLogRepositoryMock.findAllByStatusAndCallbackDataIsNotNull(SmsMessageStatus.PENDING))
        .thenReturn(List.of());

    subject.accept(new SmsDeliveryStatusPollTriggered());

    verify(befianaClientMock, never()).getDeliveryStatus(any());
    verify(smsCampaignRepositoryMock, never()).save(any());
  }

  @Test
  void delivered_status_marks_the_log_delivered() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var log = pendingLog(campaign, "cb1");
    when(smsLogRepositoryMock.findAllByStatusAndCallbackDataIsNotNull(SmsMessageStatus.PENDING))
        .thenReturn(List.of(log));
    when(befianaClientMock.getDeliveryStatus("cb1")).thenReturn(deliveryStatus("Delivered"));
    when(smsLogRepositoryMock.findAllByCampaign_IdAndStatusOrderBySentDatetimeDesc(
            "campaign1", SmsMessageStatus.PENDING))
        .thenReturn(List.of());
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(1L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(SmsMessageStatus.DELIVERED, log.getStatus());
    verify(smsLogRepositoryMock).save(log);
  }

  @Test
  void a_non_delivered_status_leaves_the_log_pending() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var log = pendingLog(campaign, "cb1");
    when(smsLogRepositoryMock.findAllByStatusAndCallbackDataIsNotNull(SmsMessageStatus.PENDING))
        .thenReturn(List.of(log));
    when(befianaClientMock.getDeliveryStatus("cb1")).thenReturn(deliveryStatus("Pending"));

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(SmsMessageStatus.PENDING, log.getStatus());
    verify(smsLogRepositoryMock, never()).save(log);
    verify(smsCampaignRepositoryMock, never()).save(any());
  }

  @Test
  void a_befiana_error_for_one_log_does_not_stop_the_sweep() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var failing = pendingLog(campaign, "cb-fail");
    var ok = pendingLog(campaign, "cb-ok");
    when(smsLogRepositoryMock.findAllByStatusAndCallbackDataIsNotNull(SmsMessageStatus.PENDING))
        .thenReturn(List.of(failing, ok));
    when(befianaClientMock.getDeliveryStatus("cb-fail"))
        .thenThrow(new BefianaException("boom", null, null));
    when(befianaClientMock.getDeliveryStatus("cb-ok")).thenReturn(deliveryStatus("Delivered"));
    when(smsLogRepositoryMock.findAllByCampaign_IdAndStatusOrderBySentDatetimeDesc(
            "campaign1", SmsMessageStatus.PENDING))
        .thenReturn(List.of());
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(1L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(SmsMessageStatus.PENDING, failing.getStatus());
    assertEquals(SmsMessageStatus.DELIVERED, ok.getStatus());
  }

  @Test
  void campaign_delivered_count_is_updated_once_nothing_is_left_pending() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var log = pendingLog(campaign, "cb1");
    when(smsLogRepositoryMock.findAllByStatusAndCallbackDataIsNotNull(SmsMessageStatus.PENDING))
        .thenReturn(List.of(log));
    when(befianaClientMock.getDeliveryStatus("cb1")).thenReturn(deliveryStatus("Delivered"));
    when(smsLogRepositoryMock.findAllByCampaign_IdAndStatusOrderBySentDatetimeDesc(
            "campaign1", SmsMessageStatus.PENDING))
        .thenReturn(List.of());
    when(smsLogRepositoryMock.countByCampaign_IdAndStatus("campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(1L);

    subject.accept(new SmsDeliveryStatusPollTriggered());

    assertEquals(1, campaign.getDeliveredCount());
    verify(smsCampaignRepositoryMock, times(1)).save(campaign);
  }

  @Test
  void campaign_is_not_updated_while_other_recipients_are_still_pending() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    var log = pendingLog(campaign, "cb1");
    var stillPendingOther = pendingLog(campaign, "cb2");
    when(smsLogRepositoryMock.findAllByStatusAndCallbackDataIsNotNull(SmsMessageStatus.PENDING))
        .thenReturn(List.of(log));
    when(befianaClientMock.getDeliveryStatus("cb1")).thenReturn(deliveryStatus("Delivered"));
    when(smsLogRepositoryMock.findAllByCampaign_IdAndStatusOrderBySentDatetimeDesc(
            "campaign1", SmsMessageStatus.PENDING))
        .thenReturn(List.of(stillPendingOther));

    subject.accept(new SmsDeliveryStatusPollTriggered());

    verify(smsCampaignRepositoryMock, never()).save(any());
  }
}
