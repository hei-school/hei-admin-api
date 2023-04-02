package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.DelayPenaltyHistory;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.repository.DelayPenaltyRepository;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.PaymentRepository;
import school.hei.haapi.service.utils.DataFormatterUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@AllArgsConstructor
public class InterestHistoryService {

  private final FeeRepository feeRepository;
  private final DelayPenaltyRepository delayPenaltyRepository;
  private final DelayPenaltyHistoryService delayPenaltyHistoryService;
  private final PaymentRepository paymentRepository;

  public int getInterestAmount(String feeId){
    Fee fee = feeRepository.getById(feeId);
    List<Payment> payments = paymentRepository.getAllPaymentByStudentIdAndFeeId(fee.getStudent().getId(),fee.getId());
    int paymentAmount = 0;
    for (Payment payment:payments) {
      if (!payment.getCreationDatetime().isAfter(fee.getDueDatetime())) {
        paymentAmount = paymentAmount + payment.getAmount();
      }
    }
    DelayPenalty configGeneral = delayPenaltyRepository.findAll(Sort.by(Sort.Direction.DESC, "lastUpdateDate")).size()>0?
            delayPenaltyRepository.findAll(Sort.by(Sort.Direction.DESC, "lastUpdateDate")).get(0):
            null;
    //TODO calcul pour configGeneral == null and else
    LocalDate interestStart = LocalDate.ofInstant(fee.getDueDatetime(), ZoneId.of("UTC")).plusDays(configGeneral.getGraceDelay());
    LocalDate interestEnd = interestStart.plusDays(configGeneral.getApplicabilityDelayAfterGrace());
    LocalDate todayDate = DataFormatterUtils.takeLocalDate();
    LocalDate lastDateOperation = todayDate.isBefore(interestEnd)?todayDate:interestEnd;
    List<DelayPenaltyHistory> delayPenaltyHistoryList = delayPenaltyHistoryService.findDelayPenaltyHistoriesByInterestStartAndEnd(interestStart,interestEnd);
    LocalDate actualDate = interestStart;
    int firstTotalAmountMinusPayment = fee.getTotalAmount() - fee.getInterestAmount() - paymentAmount;
    int amount = 0;
    int totalamount = fee.getTotalAmount() - fee.getInterestAmount() - paymentAmount;
    for (DelayPenaltyHistory delayPenaltyHistory:delayPenaltyHistoryList) {
      if (actualDate.isAfter(lastDateOperation)) {
        //calcul = calcul + " 1 /";
        break;
      } else if (actualDate.isBefore(delayPenaltyHistory.getEndDate()!=null?delayPenaltyHistory.getEndDate():todayDate)) {
        for (LocalDate i = actualDate; i.isBefore(delayPenaltyHistory.getEndDate()!=null?delayPenaltyHistory.getEndDate():todayDate); i.plusDays(delayPenaltyHistory.getTimeFrequency())) {
          if (!i.isBefore(lastDateOperation)) {
            //calcul = calcul + " 2 /";
            break;
          }else {
            amount = totalamount*delayPenaltyHistory.getInterestPercent()/100;
            //calcul =calcul + "|"+ i + "|" + totalamount + " * " + delayPenaltyHistory.getInterestPercent() + " / 100 = " + amount + "\n";
            totalamount += amount;
            actualDate =i = i.plusDays(delayPenaltyHistory.getTimeFrequency());
            //calcul =calcul + "actual dat = " + actualDate + "is aftrer: " + actualDate.isAfter(lastDateOperation) +  "is bifor : " + !i.isBefore(lastDateOperation)+   "\n" ;
          }
        }
      }
    }
    return totalamount - firstTotalAmountMinusPayment;
  }
}
