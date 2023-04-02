package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private final DelayPenaltyRepository penaltyRepository;

    public DelayPenalty getActualDelayPenalty() {
        List<DelayPenalty> penalties = penaltyRepository.findAll();
        if (penalties.isEmpty()) {
            throw new NoSuchElementException("No delay penalty found");
        }
        final int FIRST_PENALTY_INDEX = 0;
        return penalties.get(FIRST_PENALTY_INDEX);
    }

    public DelayPenalty modifyActualPenalty(DelayPenalty delayPenalty) {
        if (delayPenalty == null) {
            throw new IllegalArgumentException("Cannot modify null DelayPenalty object");
        }
        return penaltyRepository.save(delayPenalty);
    }

}
