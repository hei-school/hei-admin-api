package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.DelayPenaltyHistory;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DelayPenaltyHistoryRepository extends JpaRepository<DelayPenaltyHistory, String> {
    @Query(value = "select dph from DelayPenaltyHistory dph"
            + " where (dph.endDate  >= :interest_start or dph.endDate=null ) and dph.startDate <= :interest_end"
            + " order by dph.creationDate ASC" )
    List<DelayPenaltyHistory> findDelayPenaltyHistoriesByInterestStartAndEnd(@Param("interest_start")LocalDate start, @Param("interest_end")LocalDate end);
}
