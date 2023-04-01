package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.InterestHistory;
import school.hei.haapi.repository.DelayPenaltyRepository;
import school.hei.haapi.service.utils.DataFormatterUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
  private final DelayPenaltyRepository repository;

  private final InterestHistoryService interestHistoryService;

  private final PaymentService paymentService;

  private final DelayPenaltyHistoryService delayPenaltyHistoryService;

  public void ChangeInterestByInterestPercent(int newPercent,
                                              List<InterestHistory> interestHistories) {
    LocalDate today = DataFormatterUtils.takeLocalDate();
    InterestHistory interestHistory = interestHistories.get(interestHistories.size() - 1);
    if (interestHistory.getInterestEnd().isAfter(today)) {
      List<InterestHistory> newInterestHistories = new ArrayList<>();
      InterestHistory firstPartInterestHistory = InterestHistory.builder()
          .id(interestHistory.getId())
          .fee(interestHistory.getFee())
          .interestEnd(today.minusDays(1))
          .interestStart(interestHistory.getInterestStart())
          .interestRate(interestHistory.getInterestRate())
          .interestTimeRate(interestHistory.getInterestTimeRate())
          .build();
      newInterestHistories.add(firstPartInterestHistory);
      InterestHistory secondPartInterestHistory = InterestHistory.builder()
          .fee(interestHistory.getFee())
          .interestEnd(interestHistory.getInterestEnd())
          .interestStart(today)
          .interestRate(interestHistory.getInterestRate())
          .interestTimeRate(interestHistory.getInterestTimeRate())
          .build();
      newInterestHistories.add(secondPartInterestHistory);
      interestHistoryService.saveAll(newInterestHistories);
    }
  }

  private void ChangeInterestByApplicabilityDelayAfterGrace(int newApplicabilityDelayAfterGrace,
                                                            List<InterestHistory> interestHistories) {
    LocalDate today = DataFormatterUtils.takeLocalDate();
    if (interestHistories.get(interestHistories.size() - 1).getInterestEnd().isAfter(today)) {
      for (int i = 0; i < 2; i++) {
      }
    } else {
      for (int i = 0; i < 2; i++) {

      }
    }
  }

  public DelayPenalty getById(String delayId) {
    return repository.getById(delayId);
  }

  public List<DelayPenalty> getAll() {
    return repository.findAll();
  }

  public DelayPenalty save(DelayPenalty delayPenalties) {
    return repository.save(delayPenalties);
  }

  public DelayPenalty updateDelayPenalty(DelayPenalty newDelayPenalties) {
    DelayPenalty lastDelayPenalty = getLastUpdated();
    delayPenaltyHistoryService.updateWhenUpdatedDelayPenalty(lastDelayPenalty,newDelayPenalties);
    return repository.save(newDelayPenalties);
  }

  public DelayPenalty getFirstItem() {
    return repository.findAll().get(0);
  }

  public DelayPenalty getLastUpdated() {
    return repository.findAll(Sort.by(Sort.Direction.DESC, "lastUpdateDate")).size()>0?
            repository.findAll(Sort.by(Sort.Direction.DESC, "lastUpdateDate")).get(0):
            null;

  }

}