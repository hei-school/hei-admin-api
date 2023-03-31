package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.validator.DelayPenaltyValidator;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private final DelayPenaltyRepository repository;
    private final DelayPenaltyValidator validator;
    public DelayPenalty getDelayPenalty() {
        return repository.findFirstBy();
    }
    public DelayPenalty putDelayPenalty(DelayPenalty newValues){
            DelayPenalty currentValue = repository.findFirstBy();
            currentValue.setInterestPercent(newValues.getInterestPercent());
            currentValue.setInterestTimerate(newValues.getInterestTimerate());
            currentValue.setGraceDelay(newValues.getGraceDelay());
            currentValue.setApplicabilityDelayAfterGrace(newValues.getApplicabilityDelayAfterGrace());
            validator.accept(currentValue);
            return repository.save(currentValue);
    }
}
