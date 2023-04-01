package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;
import software.amazon.awssdk.services.eventbridge.model.DeactivateEventSourceRequest;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private final DelayPenaltyRepository repository;

    public DelayPenalty getDelayPenalty(){
        return repository.findAll().get(0);
    }
//Put DelayPenalty
    public DelayPenalty getDelayPenalty(String id){
        return repository.findById(id)
                //si il trouve l'objet
                .map(d->{
                    d.setGraceDelay(d.getGraceDelay());
                    d.setCreationDatetime(d.getCreationDatetime());
                    d.setInterestPercent(d.getInterestPercent());
                    d.setInterestTimeRate((d.getInterestTimeRate()));
                    d.setApplicabilityDelayAfterGrace(d.getApplicabilityDelayAfterGrace());
                    return repository.save(d);
                }).orElseThrow(()->new RuntimeException("l'id du delay n'a pas pu etre trouvé"));
    }
}
