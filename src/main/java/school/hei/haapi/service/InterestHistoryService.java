package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.InterestHistory;
import school.hei.haapi.repository.InterestHistoryRepository;

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
  private final FeeService feeService;

  public InterestHistory getById(String interestHistoryId) {
    return repository.getById(interestHistoryId);
  }

  public List<InterestHistory> getAllByFeeId(String feeId) {
    return repository.getByFeeId(feeId);
  }

  public List<InterestHistory> saveAll(List<InterestHistory> interestHistories) {
    return repository.saveAll(interestHistories);
  }

  public int getInterestAmount(String feeId){
    Fee fee = feeService.getById(feeId);
    List<InterestHistory> interestHistories = getAllByFeeId(feeId);
    Date todayDate = new Date();
    Date actualDate = interestHistories.get(0).getInterestStart();
    int amount = 0;
    int totalamount = fee.getTotalAmount();
    for (InterestHistory interestHistory:interestHistories) {
      if (actualDate.after(todayDate)) {
        break;
      } else if (actualDate.before(interestHistory.getInterestEnd())) {
        for (Date i = actualDate; !i.after(interestHistory.getInterestEnd()); i=getNextDate(i,interestHistory.getInterestTimeRate())) {
          if (i.after(todayDate)) {
            break;
          }else {
            amount = totalamount*interestHistory.getInterestRate()/100;
            totalamount += amount;
            actualDate = getNextDate(i,interestHistory.getInterestTimeRate());
          }
        }
      }
    }
    return (totalamount - fee.getTotalAmount());
  }
}
