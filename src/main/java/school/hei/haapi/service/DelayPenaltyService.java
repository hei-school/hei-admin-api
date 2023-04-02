package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.validator.DelayPenaltyValidator;
import school.hei.haapi.repository.DelayPenaltyRepository;
import school.hei.haapi.service.utils.InterestFeeUpdate;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private final DelayPenaltyRepository repository;
    private final DelayPenaltyValidator validator;
    private final InterestFeeUpdate interestFeeUpdate;
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
            DelayPenalty newDelayPenalty = repository.save(currentValue);
            interestFeeUpdate.updateInterestFees(newDelayPenalty);
            return newDelayPenalty;
    }
    @Scheduled(cron = "16 0 * * *")
    public void updateFeeInterest(){
        interestFeeUpdate.updateInterestFees(this.getDelayPenalty());
    }
}
