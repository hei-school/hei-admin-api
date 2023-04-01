package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.DelayPenaltyHistory;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.InterestHistory;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.DelayPenaltyRepository;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.InterestHistoryRepository;
import school.hei.haapi.repository.PaymentRepository;
import school.hei.haapi.service.utils.DataFormatterUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class InterestHistoryService {

  private static Date getNextDate(Date date,int dateNumber) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.DATE, dateNumber);//per day
    return c.getTime();
  }

  private final InterestHistoryRepository repository;
  private final FeeRepository feeRepository;
  private final DelayPenaltyRepository delayPenaltyRepository;
  private final DelayPenaltyHistoryService delayPenaltyHistoryService;
  private final PaymentRepository paymentRepository;

  public InterestHistory getById(String interestHistoryId) {
    return repository.getById(interestHistoryId);
  }

  public List<InterestHistory> getAllByFeeId(String feeId) {
    return repository.getByFeeIdOrderByInterestStartDesc(feeId);
  }

  public List<InterestHistory> saveAll(List<InterestHistory> interestHistories) {
    return repository.saveAll(interestHistories);
  }

  public int getInterestAmount1(String feeId){
    Fee fee = feeRepository.getById(feeId);
    List<InterestHistory> interestHistories = getAllByFeeId(feeId);
    LocalDate todayDate = DataFormatterUtils.takeLocalDate();
    LocalDate actualDate = interestHistories.get(0).getInterestStart();
    int amount = 0;
    int totalamount = fee.getTotalAmount();
    for (InterestHistory interestHistory:interestHistories) {
      if (actualDate.isAfter(todayDate)) {
        break;
      } else if (actualDate.isBefore(interestHistory.getInterestEnd())) {
        for (LocalDate i = actualDate; !i.isAfter(interestHistory.getInterestEnd()); i.plusDays(1)) {
          if (i.isAfter(todayDate)) {
            break;
          }else {
            amount = totalamount*interestHistory.getInterestRate()/100;
            totalamount += amount;
            actualDate = i=i.plusDays(1);

          }
        }
      }
    }
    return (totalamount - fee.getTotalAmount());
  }



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

  private int convertInterestTimeRateToDayNumber(
          school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum interestTimeRate) {
    switch (interestTimeRate) {
      case DAILY:
        return 1;
      default:
        throw new BadRequestException(
                "Unexpected delay Penalty Interest Time rate: " + interestTimeRate.getValue());
    }
  }
}
