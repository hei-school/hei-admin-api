package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.PenaltyMapper;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.service.PenaltyService;


@RestController
@AllArgsConstructor
public class PenaltyController {
    private PenaltyService penaltyService;

    private final PenaltyMapper penaltyMapper;

    @GetMapping("/delay_penalty")
    public school.hei.haapi.endpoint.rest.model.DelayPenalty getActualConfig () {
        DelayPenalty delayPenalty = penaltyService.getActualDelayPenalty();
        return penaltyMapper.toRestDelayPenalty(delayPenalty);
    }
    @PutMapping("/delay_penalty_change")
    public school.hei.haapi.endpoint.rest.model.DelayPenalty createOrUpdatePenalty(@RequestBody CreateDelayPenaltyChange restPenalty) {
        DelayPenalty delayPenalty = penaltyService.modifyActualPenalty(penaltyMapper.toDomain(restPenalty));
        return penaltyMapper.toRestDelayPenalty(delayPenalty);
    }
}
