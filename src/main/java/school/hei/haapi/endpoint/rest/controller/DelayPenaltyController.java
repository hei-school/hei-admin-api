package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;


@AllArgsConstructor
@RestController
public class DelayPenaltyController {
    private final DelayPenaltyService delayPenaltyService;

    @GetMapping("/delay_penalty")
    public DelayPenalty getCurrentDelayPenalty() {
        return delayPenaltyService.getAllDelayPenalty();
    }
}
