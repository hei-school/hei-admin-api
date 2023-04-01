package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

@RestController
@AllArgsConstructor
public class DelayPenaltyController {
  private final DelayPenaltyService delayPenaltyService;
  private final DelayPenaltyMapper delayPenaltyMapper;


  @GetMapping(value = "/delay_penalty/{id}")
  public DelayPenalty getDelayPenaltyById(@PathVariable String id) {
    return delayPenaltyMapper.toRest(delayPenaltyService.getById(id));
  }

  @GetMapping(value = "/delay_penalty")
  public DelayPenalty getDelayPenalties() {
    return delayPenaltyMapper.toRest(delayPenaltyService.getFirstItem());
  }

  @PutMapping(value = "/delay_penalty_change")
  public DelayPenalty updateDelayPenalty(@RequestBody CreateDelayPenaltyChange toWrite) {
    return delayPenaltyMapper.toRest(delayPenaltyService.updateDelayPenalty(delayPenaltyMapper.toDomain(toWrite)));
  }

  @GetMapping(value = "/delay_penalty/last")
  public DelayPenalty getLastDelayPenalties() {
    return delayPenaltyMapper.toRest(delayPenaltyService.getLastUpdated());
  }

}
