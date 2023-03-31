package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class DelayPenaltyController {
    private final DelayPenaltyService service;

    private final DelayPenaltyMapper mapper;

    @GetMapping("/delay_penalty")
    public List<DelayPenalty> getDelayPenalty(){
        return service.getAll().stream().map(mapper::ToRest).collect(Collectors.toUnmodifiableList());
    }
}
