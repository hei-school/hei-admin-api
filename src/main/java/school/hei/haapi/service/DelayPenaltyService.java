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

    public DelayPenalty getDelayPenalty(){
        int numberOfExistingDelayPenaltyConfigs = repository.countPenaltyConfigs();
        if (numberOfExistingDelayPenaltyConfigs<1){
            throw new NotFoundException("No delay penalty config found");
        }
        return repository.findAll().get(0);
    }


    public DelayPenalty saveDelayPenalty(DelayPenalty delayPenalty){
        return repository.save(delayPenalty);
    }
}
