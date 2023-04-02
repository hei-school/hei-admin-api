package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;

import java.util.Optional;

public interface DelayPenaltyRepository extends JpaRepository<DelayPenalty, Long> {
    Optional<DelayPenalty> findFirstByOrderByCreationDatetimeDesc();
}