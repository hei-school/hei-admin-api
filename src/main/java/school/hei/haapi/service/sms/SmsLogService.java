package school.hei.haapi.service.sms;

import java.util.List;
import lombok.AllArgsConstructor;
import school.hei.haapi.model.SmsLog;
import school.hei.haapi.model.SmsMessageStatus;
import school.hei.haapi.repository.SmsLogRepository;

@org.springframework.stereotype.Service
@AllArgsConstructor
public class SmsLogService {
  private final SmsLogRepository smsLogRepository;

  public List<SmsLog> getByCampaignId(String campaignId, SmsMessageStatus status) {
    return status == null
        ? smsLogRepository.findAllByCampaign_IdOrderBySentDatetimeDesc(campaignId)
        : smsLogRepository.findAllByCampaign_IdAndStatusOrderBySentDatetimeDesc(campaignId, status);
  }
}
