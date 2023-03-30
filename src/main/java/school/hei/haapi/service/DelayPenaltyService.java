package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.InterestHistory;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.DelayPenaltyRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
  private final DelayPenaltyRepository delayPenaltyRepository;

  private final InterestHistoryService interestHistoryService;

  private final PaymentService paymentService;

  public DelayPenalty getById(String delayId) {
    return delayPenaltyRepository.getById(delayId);
  }

  public List<DelayPenalty> getAll() {
    return delayPenaltyRepository.findAll();
  }

  public DelayPenalty save(DelayPenalty delayPenalties) {
    return delayPenaltyRepository.save(delayPenalties);
  }

  public DelayPenalty getFirstItem() {
    return delayPenaltyRepository.findAll().get(0);
  }
  public DelayPenalty getLastItem() {
    int indexOfLastItem = delayPenaltyRepository.findAll().size()-1;
    return delayPenaltyRepository.findAll().get(indexOfLastItem);
  }

  private Fee updateLateFee(Fee fee){
    if (!fee.getStatus().equals(school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID)){
      ZoneId zone = ZoneId.of("UTC");
      LocalDate today = LocalDate.ofInstant(Instant.now(), zone);
      LocalDate dueTime = LocalDate.ofInstant(fee.getDueDatetime(), zone);
      DelayPenalty generalRule = getAll().get(0);
      if ( Period.between(today, dueTime.plusDays(generalRule.getGraceDelay())).getDays()>=0){
        List<InterestHistory> interestHistories = interestHistoryService.getAllByFeeId(fee.getId());
        if (interestHistories.size()==0){
          //toDO change to LocalDate
          java.util.Date InterestStart = java.util.Date.from(dueTime.plusDays(generalRule.getGraceDelay()+1).atTime(12,0).toInstant(ZoneOffset.UTC));
          java.util.Date InterestEnd = java.util.Date.from(dueTime.plusDays(generalRule.getGraceDelay()+1+ generalRule.getApplicabilityDelayAfterGrace()).atTime(12,0).toInstant(ZoneOffset.UTC));
          InterestHistory toCreate = InterestHistory.builder()
                  .fee(fee)
                  .interestRate(generalRule.getInterestPercent())
                  .interestTimeRate(convertInterestTimeRateToDayNumber(generalRule.getInterestTimeRate()))

                  //.interestStart(InterestStart)
                  //.interestEnd(InterestEnd)
                  .build();
          interestHistoryService.saveAll(List.of(toCreate));
          interestHistories = interestHistoryService.getAllByFeeId(fee.getId());
        }
        fee.setTotalAmount(paymentService.computeTotalAmount(fee));
        fee.setRemainingAmount(paymentService.computeRemainingAmountV2(fee));
      }
    }
    return fee;
  }




  private int convertInterestTimeRateToDayNumber( school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum interestTimeRate){
    switch (interestTimeRate) {
      case DAILY:
        return 1;
      default:
        throw new BadRequestException(
                "Unexpected delay Penalty Interest Time rate: " + interestTimeRate.getValue());
    }
  }
}