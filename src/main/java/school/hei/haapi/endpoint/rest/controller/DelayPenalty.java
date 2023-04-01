package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
import school.hei.haapi.service.DelayPenaltyService;


@RestController
@AllArgsConstructor
public class DelayPenalty {
    private final DelayPenaltyService delayPenaltyService;
    private final DelayPenaltyMapper delayPenaltyMapper;

    @GetMapping(value = "/delay_penalty")
    public DelayPenalty getDelayPenaltyById() {
        return delayPenaltyMapper.toRest(delayPenaltyService.getById());
    }

}
