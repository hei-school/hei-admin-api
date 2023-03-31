package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class PenaltyService {
    private final DelayPenaltyRepository penaltyRepository;

    public DelayPenalty getActualDelayPenalty() {
        List<DelayPenalty> response = penaltyRepository.findAll();
        return response.get(0);
    }

    public DelayPenalty modifyActualPenalty (DelayPenalty delayPenalty) {
        return penaltyRepository.save(delayPenalty);
    }
}
