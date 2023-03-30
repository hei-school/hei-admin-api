package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class DelayPenaltyController {
    private DelayPenaltyService service;
    private DelayPenaltyMapper mapper;

    @GetMapping("/delays_penalty")
    public List<DelayPenalty> getDelayPenalty() {
        return service.getAll().stream()
                .map(mapper::toRest)
                .collect(toUnmodifiableList());
    }

}
