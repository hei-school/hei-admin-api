package school.hei.haapi.unit.sms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.SmsScheduledCampaignDispatchTriggered;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignStatus;
import school.hei.haapi.repository.SmsCampaignRepository;
import school.hei.haapi.service.event.SmsScheduledCampaignDispatchTriggeredService;
import school.hei.haapi.service.sms.SmsCampaignService;

class SmsScheduledCampaignDispatchTriggeredServiceTest {
  private final SmsCampaignRepository smsCampaignRepositoryMock = mock();
  private final SmsCampaignService smsCampaignServiceMock = mock();

  private final SmsScheduledCampaignDispatchTriggeredService subject =
      new SmsScheduledCampaignDispatchTriggeredService(
          smsCampaignRepositoryMock, smsCampaignServiceMock);

  @Test
  void no_due_campaign_dispatches_nothing() {
    when(smsCampaignRepositoryMock.findAllByStatusAndSendAtLessThanEqual(
            eq(SmsCampaignStatus.CREATED), any(Instant.class)))
        .thenReturn(List.of());

    subject.accept(new SmsScheduledCampaignDispatchTriggered());

    verify(smsCampaignServiceMock, never()).dispatchNow(any());
  }

  @Test
  void every_due_campaign_is_dispatched() {
    var due1 = SmsCampaign.builder().id("campaign1").status(SmsCampaignStatus.CREATED).build();
    var due2 = SmsCampaign.builder().id("campaign2").status(SmsCampaignStatus.CREATED).build();
    when(smsCampaignRepositoryMock.findAllByStatusAndSendAtLessThanEqual(
            eq(SmsCampaignStatus.CREATED), any(Instant.class)))
        .thenReturn(List.of(due1, due2));

    subject.accept(new SmsScheduledCampaignDispatchTriggered());

    verify(smsCampaignServiceMock, times(1)).dispatchNow("campaign1");
    verify(smsCampaignServiceMock, times(1)).dispatchNow("campaign2");
  }
}
