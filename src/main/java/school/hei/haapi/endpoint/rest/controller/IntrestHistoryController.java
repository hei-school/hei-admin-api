package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.model.DelayPenaltyHistory;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.PaymentRepository;
import school.hei.haapi.service.DelayPenaltyHistoryService;
import school.hei.haapi.service.FeeService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@AllArgsConstructor
public class IntrestHistoryController {
  private final DelayPenaltyHistoryService delayPenaltyHistoryService;
  private final FeeService feeService;
  private final FeeRepository feeRepository;
  private final PaymentRepository paymentRepository;
/*
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
   // delayPenaltyService.ChangeInterestByInterestPercent(rate, interestHistoryService.getAllByFeeId(fee_id));
    return interestHistoryService.getAllByFeeId(fee_id);
  }

 */
  @PutMapping(value = "/interest/update")
  public void updateFeesInterest() {
    feeService.updateFeesInterest();
  }

  @GetMapping(value = "/interest/fee")
  public List<Fee> updateFees() {
    return feeRepository.getNotPaidFees();
  }

  @GetMapping(value = "/interest/history")
  public List<DelayPenaltyHistory> updateFees(@RequestParam("start") int start,
                                              @RequestParam("end") int end) {
    LocalDate startDate = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC")).minusDays(start);
    LocalDate endDate = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC")).minusDays(end);

    return delayPenaltyHistoryService.findDelayPenaltyHistoriesByInterestStartAndEnd(startDate,endDate);
  }

  @GetMapping(value = "/interest/{fee_id}/{user_id}/payment")
  public List<Payment> updateFees(@PathVariable String fee_id,
                                              @PathVariable String user_id,
                                              @RequestParam("end") int end) {
    LocalDateTime endDate = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")).minusDays(end);

    return paymentRepository.getPaymentAmountByFeeAndDate(user_id,fee_id,endDate);
  }

}
