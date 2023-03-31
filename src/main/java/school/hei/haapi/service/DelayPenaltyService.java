package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.web.bind.annotation.RequestBody;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;


@Service
@AllArgsConstructor
@Slf4j
public class DelayPenaltyService {
  private final DelayPenaltyRepository delayPenaltyRepository;

  public DelayPenalty getDelayPenalty(){
    return delayPenaltyRepository.findAll().get(0);
  }

  public DelayPenalty updateDelayPenalty(@RequestBody DelayPenalty changes) {
        return delayPenaltyRepository.save(changes);
    }
}
