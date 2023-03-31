package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private final DelayPenaltyRepository repository;

    public DelayPenalty getDelayPenalty() {
        return repository.findFirstBy();
    }
    public DelayPenalty putDelayPenalty(DelayPenalty newValues){
            DelayPenalty currentValue = repository.findFirstBy();
            currentValue.setInterestPercent(newValues.getInterestPercent());
            currentValue.setInterestTimerate(newValues.getInterestTimerate());
            currentValue.setGraceDelay(newValues.getGraceDelay());
            currentValue.setApplicabilityDelayAfterGrace(newValues.getApplicabilityDelayAfterGrace());
            return repository.save(currentValue);
    }
}
