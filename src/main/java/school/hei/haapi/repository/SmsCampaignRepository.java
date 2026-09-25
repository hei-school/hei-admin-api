package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.SmsCampaign;

@Repository
public interface SmsCampaignRepository extends JpaRepository<SmsCampaign, String> {}
