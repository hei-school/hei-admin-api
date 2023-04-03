package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

import java.util.List;


@RestController
@AllArgsConstructor
public class DelayPenaltyController {

    private final DelayPenaltyService delayPenaltyService;
    private final DelayPenaltyMapper delayPenaltyMapper;

    @GetMapping("/delay_penalty")
    public school.hei.haapi.endpoint.rest.model.DelayPenalty getDelayPenalty(){
        return delayPenaltyMapper.toRestDelayPenalty(delayPenaltyService.getAll());
    }
    @PutMapping("/delay_penalty")
    public school.hei.haapi.endpoint.rest.model.DelayPenalty save(
            @RequestBody DelayPenalty toWrite
    ){
        return delayPenaltyMapper.toRestDelayPenalty(
                delayPenaltyService.save(toWrite));
    }
}
