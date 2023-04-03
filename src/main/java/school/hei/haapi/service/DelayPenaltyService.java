package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.DelayPenaltyRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    private final DelayPenaltyRepository repository;
    private final FeeService feeService;

    public DelayPenalty getDelayPenalty() {
        DelayPenalty delayPenalty = repository.findAll().get(0);
        if (delayPenalty == null) {
            throw new NotFoundException("NO DATA YET");
        }
        return delayPenalty;
    }

    public DelayPenalty saveDelayPenalty(DelayPenalty crupdate) {
        crupdate.setId(getDelayPenalty().getId());
        crupdate.setCreationDatetime(getDelayPenalty().getCreationDatetime());
        repository.save(crupdate);
        List<Fee> lateFees = feeService.getFeesByStatus(school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE);
        feeService.applyLatePenalties(lateFees);
        return crupdate;
    }

}
