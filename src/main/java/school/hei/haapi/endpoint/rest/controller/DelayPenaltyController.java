package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

@Controller
@AllArgsConstructor
public class DelayPenaltyController {
    private final DelayPenaltyService service;
    private final DelayPenaltyMapper mapper;

    @GetMapping(value = "/delay_penalty")
    public school.hei.haapi.endpoint.rest.model.DelayPenalty getPenaltyDelay() {
        return mapper.toRest(service.getDelayPenalty());
    }

    @PutMapping(value = "/delay_penalty_change")
    public school.hei.haapi.endpoint.rest.model.DelayPenalty updatePenaltyDelay(
            @RequestBody CreateDelayPenaltyChange delayPenalty
    ) {
        DelayPenalty crupdate= mapper.toDomainDelayPenality(delayPenalty);
        return mapper.toRest(service.saveDelayPenalty(crupdate));
    }
}
