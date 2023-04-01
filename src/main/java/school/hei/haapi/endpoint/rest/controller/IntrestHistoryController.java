package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.InterestHistory;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.service.DelayPenaltyService;
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.InterestHistoryService;

import java.util.List;

@RestController
@AllArgsConstructor
public class IntrestHistoryController {
  private final InterestHistoryService interestHistoryService;
  private final DelayPenaltyService delayPenaltyService;
  private final FeeService feeService;
  private final FeeRepository feeRepository;

  @GetMapping(value = "/interest/{fee_id}/fee")
  public List<InterestHistory> getInterest(@PathVariable String fee_id) {
    return interestHistoryService.getAllByFeeId(fee_id);
  }
  @GetMapping(value = "/interest/{fee_id}/fee/amount")
  public int getInterestAmount(@PathVariable String fee_id) {
    return interestHistoryService.getInterestAmount(fee_id);
  }
  @PutMapping(value = "/interest/{fee_id}/fee/{rate}")
  public List<InterestHistory> getInterest(@PathVariable String fee_id, @PathVariable int rate) {
    delayPenaltyService.ChangeInterestByInterestPercent(rate, interestHistoryService.getAllByFeeId(fee_id));
    return interestHistoryService.getAllByFeeId(fee_id);
  }

  @PutMapping(value = "/interest/update")
  public void updateFeesInterest() {
    feeService.updateFeesInterest();
  }

  @GetMapping(value = "/interest/fee")
  public List<Fee> updateFees() {
    return feeRepository.getNotPaidFees();
  }


}
