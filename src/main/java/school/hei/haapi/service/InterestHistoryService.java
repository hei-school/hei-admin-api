package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.InterestHistory;
import school.hei.haapi.repository.InterestHistoryRepository;
import school.hei.haapi.service.utils.DataFormatterUtils;

import java.time.LocalDate;
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
    return repository.getByFeeIdOrderByInterestStartDesc(feeId);
  }

  public List<InterestHistory> saveAll(List<InterestHistory> interestHistories) {
    return repository.saveAll(interestHistories);
  }

  public int getInterestAmount(String feeId){
    Fee fee = feeService.getById(feeId);
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
}
