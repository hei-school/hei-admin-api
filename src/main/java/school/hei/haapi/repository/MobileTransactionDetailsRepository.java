package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.MobileTransactionDetails;

@Repository
public interface MobileTransactionDetailsRepository
    extends JpaRepository<MobileTransactionDetails, String> {
  MobileTransactionDetails findByPspTransactionRef(String pspRef);
}
