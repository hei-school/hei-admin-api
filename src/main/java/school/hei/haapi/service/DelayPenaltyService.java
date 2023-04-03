package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.validator.DelayPenaltyValidator;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private DelayPenaltyRepository repository;
    private DelayPenaltyValidator validator;

    public DelayPenalty getDelayPenalty() {
        return repository.findAll().get(0);
    }

    public DelayPenalty updateDelayPenalty(DelayPenalty newDelayPenalty) {
        validator.accept(newDelayPenalty);
        repository.save(newDelayPenalty);
        return newDelayPenalty;
    }
    public DelayPenalty getLastUpdated() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "lastUpdateDate")).size()>0?
                repository.findAll(Sort.by(Sort.Direction.DESC, "lastUpdateDate")).get(0):
                null;
    }


}