package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.PaymentNumberSequence;

import java.util.Optional;

@Repository
public interface PaymentNumberSequenceRepository extends JpaRepository<PaymentNumberSequence, String> {
    Optional<PaymentNumberSequence> findFirstByDatePartOrderBySequenceNumberDesc(String datePart);
}
