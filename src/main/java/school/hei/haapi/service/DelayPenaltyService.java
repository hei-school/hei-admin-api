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

    private final DelayPenaltyRepository repository;

    public DelayPenalty getCurrentDelay() {
        return repository.findCurrentDelayPenalty().get(0);
    }

    public DelayPenalty saveCurrentDelay(DelayPenalty delayPenalty) {
        return repository.saveAndFlush(delayPenalty);
    }
}
