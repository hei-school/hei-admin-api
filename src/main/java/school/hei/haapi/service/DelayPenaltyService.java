package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private final DelayPenaltyRepository repository;
    public DelayPenalty getDelayPenalty() {
        return repository.findAll().get(0);
    }

    public DelayPenalty createDelayPenaltyChange(CreateDelayPenaltyChange createDelayPenaltyChange) {
        DelayPenalty current = getDelayPenalty();
        DelayPenalty updated = DelayPenalty.builder()
                .id(current.getId())
                .interestPercent(createDelayPenaltyChange.getInterestPercent())
                .interestTimerate(
                        Objects.equals(createDelayPenaltyChange.getInterestTimerate(), current.getInterestTimerate()) ?
                                current.getInterestTimerate() : null
                        )
                .graceDelay(createDelayPenaltyChange.getGraceDelay())
                .applicabilityDelayAfterGrace(createDelayPenaltyChange.getApplicabilityDelayAfterGrace())
                .build();
        return repository.saveAll(List.of(updated)).get(0);
    }
}
