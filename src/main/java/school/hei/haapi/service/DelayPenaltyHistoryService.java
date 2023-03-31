package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenaltyHistory;
import school.hei.haapi.repository.DelayPenaltyHistoryRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyHistoryService {
  private final DelayPenaltyHistoryRepository delayPenaltyHistoryRepository;

  public DelayPenaltyHistory getById(String delayHistory) {
    return delayPenaltyHistoryRepository.getById(delayHistory);
  }

  public DelayPenaltyHistory save(DelayPenaltyHistory delayPenaltyHistory) {
    return delayPenaltyHistoryRepository.save(delayPenaltyHistory);
  }
}
