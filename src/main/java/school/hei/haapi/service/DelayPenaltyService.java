package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.Payment;
import school.hei.haapi.repository.DelayPenaltyRepository;
import school.hei.haapi.repository.PaymentRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class DelayPenaltyService {
    private final DelayPenaltyRepository repository;
    public DelayPenalty getAll() {
        return repository.findAll().get(0);
    }
    @Transactional
    public DelayPenalty save(DelayPenalty delayPenalties) {
        repository.save(delayPenalties);
        return repository.getById(delayPenalties.getId());
    }

}
