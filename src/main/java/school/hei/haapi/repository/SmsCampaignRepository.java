package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.SmsCampaign;
import school.hei.haapi.model.SmsCampaignStatus;

@Repository
public interface SmsCampaignRepository extends JpaRepository<SmsCampaign, String> {

  List<SmsCampaign> findAllByStatusAndSendAtLessThanEqual(SmsCampaignStatus status, Instant now);
}
