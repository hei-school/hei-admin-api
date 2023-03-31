package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.DelayPenalty;

import java.util.List;

@Repository
public interface DelayPenaltyRepository extends JpaRepository<DelayPenalty, String> {

    @Query(value = "select * from delay_penalty order by creation_datetime desc",nativeQuery = true)
    List<DelayPenalty> findCurrentDelayPenalty();
}
