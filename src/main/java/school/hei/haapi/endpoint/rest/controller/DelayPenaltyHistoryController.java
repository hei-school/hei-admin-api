package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.DelayPenaltyHistory;
import school.hei.haapi.service.DelayPenaltyHistoryService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@AllArgsConstructor
public class DelayPenaltyHistoryController {
  private final DelayPenaltyHistoryService delayPenaltyHistoryService;

  @GetMapping("/fee/delay-penalty-history")
  public List<DelayPenaltyHistory> getFees(
          @RequestParam("start_from_now") int startFromNow,
          @RequestParam("end_from_now") int endFromNow) {
    LocalDate start = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC")).plusDays(startFromNow);
    LocalDate end = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC")).plusDays(endFromNow);
    if (startFromNow<0){
      start = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC")).minusDays(startFromNow*-1);
    }
    if (endFromNow<0){
      end = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC")).minusDays(endFromNow*-1);
    }

    return delayPenaltyHistoryService.findDelayPenaltyHistoriesByInterestStartAndEnd(start,end);
  }

}
