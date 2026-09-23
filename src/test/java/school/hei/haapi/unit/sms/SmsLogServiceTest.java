package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsLog;
import school.hei.haapi.model.SmsMessageStatus;
import school.hei.haapi.repository.SmsLogRepository;
import school.hei.haapi.service.sms.SmsLogService;

class SmsLogServiceTest {
  private final SmsLogRepository smsLogRepositoryMock = mock();
  private final SmsLogService subject = new SmsLogService(smsLogRepositoryMock);

  private SmsLog log() {
    var campaign = SmsCampaign.builder().id("campaign1").build();
    return SmsLog.builder().id("log1").campaign(campaign).phoneNumber("321111111").build();
  }

  @Test
  void no_status_filter_lists_every_log_for_the_campaign() {
    when(smsLogRepositoryMock.findAllByCampaign_IdOrderBySentDatetimeDesc("campaign1"))
        .thenReturn(List.of(log()));

    var result = subject.getByCampaignId("campaign1", null);

    assertEquals(1, result.size());
  }

  @Test
  void a_status_filter_is_forwarded_to_the_repository() {
    when(smsLogRepositoryMock.findAllByCampaign_IdAndStatusOrderBySentDatetimeDesc(
            "campaign1", SmsMessageStatus.DELIVERED))
        .thenReturn(List.of(log()));

    var result = subject.getByCampaignId("campaign1", SmsMessageStatus.DELIVERED);

    assertEquals(1, result.size());
  }
}
