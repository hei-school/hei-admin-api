package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.model.validator.DelayPenaltyValidator;
import school.hei.haapi.service.DelayPenaltyService;

@RestController
@AllArgsConstructor
public class DelayPenaltyController {
    private final DelayPenaltyService service;
    private final DelayPenaltyMapper mapper;
    private final DelayPenaltyValidator validator;

    @PutMapping(value = "/delay_penalty_change")
    public DelayPenalty updateDelayPenalty(
        @RequestBody CreateDelayPenaltyChange delayPenaltyChange) {
        validator.accept(delayPenaltyChange);
        school.hei.haapi.model.DelayPenalty toSave =
            mapper.toDomain(delayPenaltyChange);
        return mapper.toRest(service.updateDelayPenaltyChange(toSave));
    }
}
