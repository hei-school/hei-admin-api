package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
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
  public List<DelayPenalty> getDelayPenalties() {
    return delayPenaltyService.getAll().stream()
        .map(delayPenaltyMapper::toRest)
        .collect(toUnmodifiableList());
  }

  /*
  @PutMapping(value = "/delay_penalty_change")
  public List<DelayPenalty> createOrUpdateGroups(@RequestBody List<DelayPenalty> toWrite) {
    var saved = delayPenaltyService.saveAll(toWrite.stream()
        .map(delayPenaltyMapper::toDomain)
        .collect(toUnmodifiableList()));
    return saved.stream()
        .map(delayPenaltyMapper::toRest)
        .collect(toUnmodifiableList());
  }

   */
}
