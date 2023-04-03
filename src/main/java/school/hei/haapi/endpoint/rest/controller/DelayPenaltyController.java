package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
<<<<<<< HEAD
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
=======
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DelayPenaltyMapper;
import school.hei.haapi.endpoint.rest.model.CreateDelayPenaltyChange;
>>>>>>> b09ac50cd23193878f556194a773ca93e3726b58
import school.hei.haapi.endpoint.rest.model.DelayPenalty;
import school.hei.haapi.service.DelayPenaltyService;

@RestController
@AllArgsConstructor
public class DelayPenaltyController {
  private final DelayPenaltyService delayPenaltyService;
  private final DelayPenaltyMapper delayPenaltyMapper;

  @GetMapping("/delay_penalty")
  private DelayPenalty getDelayPenalty (){
    return delayPenaltyMapper.toRest(delayPenaltyService.getCurrentDelayPenalty());
  }
<<<<<<< HEAD
=======

  @PutMapping("/delay_penalty_change")
  private DelayPenalty crupdateDelayPenalty(@RequestBody CreateDelayPenaltyChange toCrupdate){
    return delayPenaltyMapper.toRest(delayPenaltyService.crupdateDelayPenalty(
        delayPenaltyMapper.toDomain(toCrupdate)));
  }

>>>>>>> b09ac50cd23193878f556194a773ca93e3726b58
}
