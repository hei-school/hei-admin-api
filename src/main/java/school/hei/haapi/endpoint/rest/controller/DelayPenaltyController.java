package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

@RestController
@AllArgsConstructor
public class DelayPenaltyController {

    private final DelayPenaltyService service;

    private final DelayPenaltyMapper mapper;

    @GetMapping("/delay_penalty")
    public DelayPenalty getCurrentDelayPenalty() {
        return mapper.toRest(service.getCurrentDelay());
    }

    @PutMapping("/delay_penalty_change")
    public DelayPenalty saveDelayPenalty(@RequestBody CreateDelayPenaltyChange createDelayPenalty) {
        return mapper.toRest(service.saveCurrentDelay(mapper.toDomain(createDelayPenalty)));
    }
}
