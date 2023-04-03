package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.DelayPenaltyRepository;


@Service
@AllArgsConstructor
@Slf4j
public class DelayPenaltyService {
    private final DelayPenaltyRepository delayPenaltyRepository;

    public DelayPenalty getDelayPenalty() {
        int numberOfExistingDelayPenaltyConfigs = delayPenaltyRepository.countPenaltyConfigs();
        if (numberOfExistingDelayPenaltyConfigs < 1) {
            throw new NotFoundException("No delay penalty config found");
        }
        return delayPenaltyRepository.findAll().get(0);
    }

    public DelayPenalty updateDelayPenalty(DelayPenalty changes) {
        return delayPenaltyRepository.save(changes);
    }
}
