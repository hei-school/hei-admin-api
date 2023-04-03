package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

@RestController
@AllArgsConstructor
public class DelayPenaltyController {

    private final DelayPenaltyService service;
    private final DelayPenaltyMapper delayPenaltyMapper;

    @GetMapping("/delay_penalty")
    public DelayPenalty getDelayPenalty(){
        return delayPenaltyMapper.toRest(service.getDelayPenalty());
    }

    @PutMapping("/delay_penalty_change")
    public DelayPenalty putDelayPenalty(DelayPenalty changes) {
        var saved = service.updateDelayPenalty(delayPenaltyMapper.toDomain(changes));
        return delayPenaltyMapper.toRest(saved);
    }
}