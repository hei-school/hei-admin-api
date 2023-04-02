package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    public DelayPenaltyRepository delayPenaltyRepository;

    public DelayPenalty getCurrentDelayPenalty() {
        List<DelayPenalty> delayPenalties = delayPenaltyRepository.findAll();
        return delayPenalties.get(delayPenalties.size() -1 );
    }
}
