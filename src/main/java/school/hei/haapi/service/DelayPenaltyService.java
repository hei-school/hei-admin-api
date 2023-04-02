package school.hei.haapi.service;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
  private final DelayPenaltyRepository repository;

  public DelayPenalty getDelayPenalty() {
    Optional<DelayPenalty> optional = repository.findFirstByOrderByCreationDatetime();
    return optional.orElseThrow(() -> new NotFoundException("Delay penalty not found"));
  }

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
          .graceDelay(5)
          .applicabilityDelayAfterGrace(10)
          .interestPercent(2)
          .build();

      return repository.save(default_delayPenalty);
    });
  }
}
