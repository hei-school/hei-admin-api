package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.controller.DelayPenalty;

@Repository
public interface DelayPenaltyRepository extends JpaRepository<DelayPenalty, String> {
}
