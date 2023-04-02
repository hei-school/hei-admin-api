package school.hei.haapi.endpoint.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.DelayPenaltyChange;
import school.hei.haapi.service.DelayPenaltyService;

@RestController
public class DelayPenaltyController {
    @Autowired
    private DelayPenaltyService delayPenaltyService;

    @GetMapping("/delay_penalty/{delayPenaltyId}")
    public ResponseEntity<DelayPenalty> getDelayPenalty(@PathVariable String delayPenaltyId) {
        DelayPenalty delayPenalty = delayPenaltyService.getDelayPenalty(delayPenaltyId);
        return ResponseEntity.ok(delayPenalty);
    }

    @PutMapping("/delay_penalty_change")
    public DelayPenalty changeDelayPenaltyConfiguration(@RequestBody DelayPenaltyChange delayPenaltyChange) {
        return delayPenaltyService.changeDelayPenaltyConfiguration(delayPenaltyChange);
    }
}
