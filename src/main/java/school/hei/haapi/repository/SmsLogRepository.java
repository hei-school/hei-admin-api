package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.SmsLog;
import school.hei.haapi.model.SmsMessageStatus;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, String> {
  List<SmsLog> findAllByCampaign_IdOrderBySentDatetimeDesc(String campaignId);

  List<SmsLog> findAllByCampaign_IdAndStatusOrderBySentDatetimeDesc(
      String campaignId, SmsMessageStatus status);

  List<SmsLog> findAllByStatusAndCallbackDataIsNotNull(SmsMessageStatus status);

  long countByCampaign_IdAndStatus(String campaignId, SmsMessageStatus status);
}
