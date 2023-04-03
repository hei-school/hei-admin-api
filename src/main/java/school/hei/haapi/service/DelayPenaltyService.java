package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Service
@AllArgsConstructor
@Slf4j
public class DelayPenaltyService {
    private final DelayPenaltyRepository repository;
    private final FeeService feeService;

    public DelayPenalty get() {
        DelayPenalty current = repository.findAll().get(0);
        DelayPenalty c = repository.getByStudentIdAndStatus("student1_id",DelayPenalty.StatusEnum.SPECIFIC);
        log.info(c.toString());
        if (current == null) {
            throw new NotFoundException("No data to display");
        }
        return current;
    }

    public DelayPenalty save(DelayPenalty toSave) {
        toSave.setId(getCurrentPenalty().getId());
        toSave.setCreationDatetime(getCurrentPenalty().getCreationDatetime());
        repository.save(toSave);
        feeService.automateApplyInterest();
        return toSave;
    }

    public DelayPenalty getCurrentPenalty() {
        DelayPenalty delayPenalty = repository.findAll().get(0);
        if(delayPenalty == null) {
            DelayPenalty current = DelayPenalty.builder()
                    .interestPercent(2)
                    .applicabilityDelayAfterGrace(10)
                    .graceDelay(0)
                    .build();
            return repository.save(current);
        }
        return delayPenalty;
    }
}