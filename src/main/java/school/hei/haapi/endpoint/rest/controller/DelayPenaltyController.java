package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
import school.hei.haapi.endpoint.rest.mapper.FeeMapper;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.DelayPenaltyService;
import school.hei.haapi.service.FeeService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class DelayPenaltyController {
    private DelayPenaltyService delayPenaltyService;
    private DelayPenaltyMapper delayPenaltyMapper;

    @GetMapping("/delay_penalty")
    public DelayPenalty getCurrentDelayPenalty() {
        return delayPenaltyMapper.toRest(delayPenaltyService.getCurrentDelayPenalty());
    }


    @PutMapping("/delay_penalty_change")
    public DelayPenalty changeCurrentDelayPenalty(@RequestBody CreateDelayPenaltyChange delayPenaltyChange) {
        return delayPenaltyMapper.toRest(delayPenaltyService.saveDelayPenalty(delayPenaltyMapper.toDomain(delayPenaltyChange)));
    }
}
