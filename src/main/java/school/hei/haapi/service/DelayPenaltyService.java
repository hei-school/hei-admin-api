package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Service
@AllArgsConstructor
@Slf4j
public class DelayPenaltyService {
  private DelayPenaltyRepository repository;

  public DelayPenalty getOneOrderByCreationDatetimeDesc() {
    return repository.findFirstByOrderByCreationDatetimeDesc();
  }
}