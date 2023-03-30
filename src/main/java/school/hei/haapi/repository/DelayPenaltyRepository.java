package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.controller.DelayPenaltyController;

@Repository
public interface DelayPenaltyRepository extends JpaRepository<DelayPenaltyController, String> {
}
