package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;


@AllArgsConstructor
@RestController
public class DelayPenaltyController {
    private final DelayPenaltyService delayPenaltyService;

    @GetMapping("/delay_penalty")
    public DelayPenalty getDelayPenalty() {
        return delayPenaltyService.getDelayPenalty();
    }

    @PutMapping("/delay_penalty_change")
    public DelayPenalty createDelayPenaltyChange(@RequestBody CreateDelayPenaltyChange createDelayPenaltyChange) {
        return delayPenaltyService.createDelayPenaltyChange(createDelayPenaltyChange);
    }
}
