package school.hei.haapi.service;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Complaint;
import school.hei.haapi.model.ComplaintStatus;
import school.hei.haapi.model.ComplaintStatusHistory;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.ComplaintStatusHistoryRepository;
import school.hei.haapi.repository.dao.ComplaintStatusHistoryDao;

@Service
@AllArgsConstructor
public class ComplaintStatusHistoryService {
  private final ComplaintStatusHistoryRepository complaintStatusHistoryRepository;
  private final ComplaintStatusHistoryDao complaintStatusHistoryDao;

  public ComplaintStatusHistory save(
      Complaint complaint, ComplaintStatus status, String comment, User changedBy) {
    return complaintStatusHistoryRepository.save(
        ComplaintStatusHistory.builder()
            .complaint(complaint)
            .status(status)
            .comment(comment)
            .changedBy(changedBy)
            .build());
  }

  public List<ComplaintStatusHistory> getHistory(
      String complaintId, Instant from, Instant to, Pageable pageable) {
    return complaintStatusHistoryDao.findByCriteria(complaintId, from, to, pageable);
  }
}
