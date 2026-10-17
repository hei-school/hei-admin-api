package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.SmsCampaignBatch;

@Repository
public interface SmsCampaignBatchRepository extends JpaRepository<SmsCampaignBatch, String> {}
