package school.hei.haapi.service;

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
        DelayPenalty delayPenalty = repository.findAll().get(0);
        if (delayPenalty == null) {
            throw new NotFoundException("No data to display");
        }
        return delayPenalty;
    }
}
