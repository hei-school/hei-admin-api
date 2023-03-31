package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private DelayPenaltyRepository repository;

    public DelayPenalty getDelayPenalty() {
        return repository.findAll().get(0);
    }
}
