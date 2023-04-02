package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import school.hei.haapi.endpoint.rest.mapper.PenaltyMapper;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;


@RestController
@AllArgsConstructor
public class DelayPenaltyController {
    private DelayPenaltyService delayPenaltyService;

    private final PenaltyMapper penaltyMapper;

    @GetMapping("/delay_penalty")
    public school.hei.haapi.endpoint.rest.model.DelayPenalty getActualConfig() {
        DelayPenalty actualDelayPenalty = delayPenaltyService.getActualDelayPenalty();
        if (actualDelayPenalty == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No delay penalty found");
        }
        return penaltyMapper.toRestDelayPenalty(actualDelayPenalty);
    }

    @PutMapping("/delay_penalty_change")
    public school.hei.haapi.endpoint.rest.model.DelayPenalty createOrUpdatePenalty(@RequestBody CreateDelayPenaltyChange restPenalty) {
        DelayPenalty delayPenalty = delayPenaltyService.modifyActualPenalty(penaltyMapper.toDomain(restPenalty));
        return penaltyMapper.toRestDelayPenalty(delayPenalty);
    }
}
