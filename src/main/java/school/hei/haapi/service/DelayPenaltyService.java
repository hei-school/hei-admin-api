package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.controller.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.repository.DelayPenaltyRepository;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
    public DelayPenaltyRepository delayPenaltyRepository;

    public DelayPenalty getById(String id) {
        return (delayPenaltyRepository.getById(id));
    }
}
