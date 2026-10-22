package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Complaint;
import school.hei.haapi.model.ComplaintStatus;
import school.hei.haapi.model.ComplaintStatusHistory;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.ComplaintStatusHistoryRepository;

@Service
@AllArgsConstructor
public class ComplaintStatusHistoryService {
  private final ComplaintStatusHistoryRepository complaintStatusHistoryRepository;

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

  public List<ComplaintStatusHistory> getHistory(String complaintId, Sort sort) {
    return complaintStatusHistoryRepository.findByComplaintIdOrderByDatetime(complaintId, sort);
  }
}
