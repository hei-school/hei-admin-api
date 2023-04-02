package school.hei.haapi.endpoint.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

@RestController
@RequestMapping("/delay_penalty")
public class DelayPenaltyController {
    @Autowired
    private DelayPenaltyService delayPenaltyService;

    @GetMapping("/{delayPenaltyId}")
    public ResponseEntity<DelayPenalty> getDelayPenalty(@PathVariable String delayPenaltyId) {
        DelayPenalty delayPenalty = delayPenaltyService.getDelayPenalty(delayPenaltyId);
        return ResponseEntity.ok(delayPenalty);
    }
}
