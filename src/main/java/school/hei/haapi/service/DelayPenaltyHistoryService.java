package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenaltyHistory;
import school.hei.haapi.repository.DelayPenaltyHistoryRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class DelayPenaltyHistoryService {
  private final DelayPenaltyHistoryRepository repository;

  public DelayPenaltyHistory getById(String delayHistory) {
    return repository.getById(delayHistory);
  }

  public DelayPenaltyHistory save(DelayPenaltyHistory delayPenaltyHistory) {
    return repository.save(delayPenaltyHistory);
  }

  public List<DelayPenaltyHistory> findDelayPenaltyHistoriesByInterestStartAndEnd(LocalDate InterestStart, LocalDate InterestEnd){
    return repository.findDelayPenaltyHistoriesByInterestStartAndEnd(InterestStart,InterestEnd);
  }
}
