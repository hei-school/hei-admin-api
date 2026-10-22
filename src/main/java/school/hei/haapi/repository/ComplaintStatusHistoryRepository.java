package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.ComplaintStatusHistory;

@Repository
public interface ComplaintStatusHistoryRepository
    extends JpaRepository<ComplaintStatusHistory, String> {
  @Query(
      "select h from ComplaintStatusHistory h"
          + " where h.status = school.hei.haapi.model.ComplaintStatus.IN_PROGRESS"
          + " and h.datetime < :threshold"
          + " and h.complaint.status = school.hei.haapi.model.ComplaintStatus.IN_PROGRESS"
          + " and h.complaint.reminderSentAt is null")
  List<ComplaintStatusHistory> findStuckInProgressSince(@Param("threshold") Instant threshold);
}
