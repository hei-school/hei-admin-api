package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

@RestController
@AllArgsConstructor
public class DelayPenaltyController {
    private final DelayPenaltyService delayPenaltyService;
    private final DelayPenaltyMapper delayPenaltyMapper;

    @GetMapping("/delay_penalty")
    public DelayPenalty getDelayPenalty() {
        return delayPenaltyMapper.toRest(delayPenaltyService.getDelayPenalty());
    }
}
