package school.hei.haapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.PaymentNumberSequence;

@Repository
public interface PaymentNumberSequenceRepository
    extends JpaRepository<PaymentNumberSequence, String> {
  Optional<PaymentNumberSequence> findFirstByYearMonthOrderBySequenceNumberDesc(String yearMonth);
}
