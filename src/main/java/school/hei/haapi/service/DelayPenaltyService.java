package school.hei.haapi.service;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private DelayPenaltyRepository repository;

    public DelayPenalty getDelayPenaltyGlobalConf() {
        return repository.findByUserId(null).get();
    }

    public DelayPenalty updateDelayPenaltyChange(DelayPenalty delayPenalty) {
        return repository.save(delayPenalty);
    }

    public Optional<DelayPenalty> getIndividualDelayPenalty(String studentId) {
        return repository.findByUserId(studentId);
    }
}
