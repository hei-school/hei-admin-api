package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.Penalty;
import school.hei.haapi.repository.PenaltyRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PenaltyService {
    private final PenaltyRepository penaltyRepository;

    public List<Penalty> getAll() {
        return penaltyRepository.findAll();
    }

    public Penalty update(CreateDelayPenaltyChange restPenalty) {
        Optional<Penalty> optionalPenalty = penaltyRepository.findAll().stream().findFirst();
        if (optionalPenalty.isPresent()) {
            Penalty firstPenalty = optionalPenalty.get();
            return penaltyRepository.save(Penalty.builder()
                    .id(firstPenalty.getId())
                            .interestPercent(restPenalty.getInterestPercent())
                            .interestTimeRate(Penalty.TimeRate.valueOf(restPenalty.getInterestTimerate().getValue()))
                            .graceDelay(restPenalty.getGraceDelay())
                            .applicabilityDelayAfterGrace(restPenalty.getApplicabilityDelayAfterGrace())
                            .creationDateTime(firstPenalty.getCreationDateTime())
                    .build());
        } else {
            throw new NoSuchElementException("Aucune pénalité trouvée à modifier");
        }
    }
}
