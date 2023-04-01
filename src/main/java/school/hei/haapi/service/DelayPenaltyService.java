package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
public class DelayPenaltyService {

    private DelayPenaltyRepository delayPenaltyRepository;

    public DelayPenalty updateDelayPenalty(school.hei.haapi.endpoint.rest.model.DelayPenalty delayPenalty) {
        Optional<school.hei.haapi.model.DelayPenalty> delayPenaltyOptional = delayPenaltyRepository.findById("1");

        if (delayPenaltyOptional.isPresent()) {
            school.hei.haapi.model.DelayPenalty toUpdate = delayPenaltyOptional.get();
            DelayPenalty.setInterestPercent(delayPenalty.getInterestPercent());
            DelayPenalty.setInterestTimeRate(delayPenalty.getInterestTimerate());
            DelayPenalty.setGraceDelay(delayPenalty.getGraceDelay());
            DelayPenalty.setApplicabilityDelayAfterGrace(delayPenalty.getGraceDelay());
            delayPenaltyRepository.save(toUpdate);
        } else {
            // Création d'un nouvel objet DelayPenalty avec l'ID 1
            DelayPenalty toCreate = new DelayPenalty();
            DelayPenalty.setInterestPercent(delayPenalty.getInterestPercent());
            DelayPenalty.setInterestTimeRate(delayPenalty.getInterestTimerate());
            DelayPenalty.setGraceDelay(delayPenalty.getGraceDelay());
            DelayPenalty.setApplicabilityDelayAfterGrace(delayPenalty.getGraceDelay());
            delayPenaltyRepository.save(toCreate);
        }
        return delayPenaltyRepository.findById("1").get();
    }


}
