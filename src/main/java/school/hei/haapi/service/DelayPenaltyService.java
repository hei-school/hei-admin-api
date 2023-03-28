package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
  private final DelayPenaltyRepository delayPenaltyRepository;

  public DelayPenalty getById(String delayId) {
    return delayPenaltyRepository.getById(delayId);
  }

  public List<DelayPenalty> getAll() {
    return delayPenaltyRepository.findAll();
  }

  public List<DelayPenalty> saveAll(List<DelayPenalty> delayPenalties) {
    return delayPenaltyRepository.saveAll(delayPenalties);
  }
}