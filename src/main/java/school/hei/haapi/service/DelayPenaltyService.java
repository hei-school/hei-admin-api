package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Group;
import school.hei.haapi.repository.DelayPenaltyRepository;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.Payment;
import school.hei.haapi.repository.DelayPenaltyRepository;
import school.hei.haapi.repository.PaymentRepository;

import java.util.List;
import java.util.Optional;

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
        DelayPenalty delayPenalty = getAll();
        delayPenalty.setGraceDelay(delayPenalties.getGraceDelay());
        delayPenalty.setId(delayPenalties.getId());
        delayPenalty.setApplicabilityDelayAfterGrace(delayPenalties.getApplicabilityDelayAfterGrace());
        delayPenalty.setInterestPercent(delayPenalties.getInterestPercent());
        delayPenalty.setInterestTimerate(delayPenalties.getInterestTimerate());
        delayPenalty.setCreationDatetime(delayPenalties.getCreationDatetime());
        repository.save(delayPenalty);
        return repository.getById(delayPenalties.getId());
    }
}
