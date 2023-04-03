package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.controller.DelayPenaltyController;
import school.hei.haapi.model.DelayPenalty;

import java.util.List;

import static org.hibernate.loader.Loader.SELECT;

@Repository
public interface DelayPenaltyRepository extends JpaRepository<DelayPenalty, String> {

    @Query("SELECT e FROM DelayPenalty e ORDER BY e.creationDatetime DESC")
    List<DelayPenalty> findMostRecent(Pageable pageable);
}
