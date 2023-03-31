package school.hei.haapi.service;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
  private final DelayPenaltyRepository repository;

  /**
   * update the current configuration.
   * <br>
   * We first need to define a default configuration
   */
  public DelayPenalty save(DelayPenalty delayPenalty) {
    delayPenalty.setId(getCurrentDelayPenalty().getId());
    return repository.save(delayPenalty);
  }

  /**
   * fetches the current delay_penalty if there is, creates new one otherwise
   */
  public DelayPenalty getCurrentDelayPenalty() {
    Optional<DelayPenalty> optional = repository.findFirstByOrderByCreationDatetime();
    return optional.orElseGet(() -> {
      DelayPenalty default_delayPenalty = DelayPenalty.builder()
          .graceDelay(2)
          .applicabilityDelayAfterGrace(5)
          .interestPercent(2)
          .build();

      return repository.save(default_delayPenalty);
    });
  }
}
