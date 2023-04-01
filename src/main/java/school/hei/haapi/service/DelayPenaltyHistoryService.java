package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.DelayPenaltyHistory;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.DelayPenaltyHistoryRepository;
import school.hei.haapi.service.utils.DataFormatterUtils;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class DelayPenaltyHistoryService {
  private final DelayPenaltyHistoryRepository repository;

  public DelayPenaltyHistory getById(String delayHistory) {
    return repository.getById(delayHistory);
  }

  public DelayPenaltyHistory save(DelayPenaltyHistory delayPenaltyHistory) {
    return repository.save(delayPenaltyHistory);
  }

  public List<DelayPenaltyHistory> saveAll(List<DelayPenaltyHistory> delayPenaltyHistories) {
    return repository.saveAll(delayPenaltyHistories);
  }

  @Transactional
  public void updateWhenUpdatedDelayPenalty(DelayPenalty newDelayPenalty) {
    DelayPenaltyHistory lastDelayPenaltyHistoryToModify = getLastItem();
    lastDelayPenaltyHistoryToModify.setEndDate(DataFormatterUtils.takeLocalDate());
    DelayPenaltyHistory newDelayPenaltyHistory = DelayPenaltyHistory.builder()
            .delayPenalty(newDelayPenalty)
            .interestPercent(newDelayPenalty.getInterestPercent())
            .timeFrequency(dayFrequency(Objects.requireNonNull(newDelayPenalty.getInterestTimeRate())))
            .startDate(DataFormatterUtils.takeLocalDate())
            .endDate(null)
            .creationDate(Instant.now()).build();
    saveAll(List.of(lastDelayPenaltyHistoryToModify,newDelayPenaltyHistory));
  }

  private DelayPenaltyHistory getLastItem() {
    return repository.findAll(Sort.by(Sort.Direction.DESC, "creationDate")).get(0);
  }

  private DelayPenaltyHistory getFirstItem() {
    return repository.findAll().get(0);
  }

  private DelayPenaltyHistory getBeforeLastItem() {
    int indexOfLastItem = repository.findAll().size() - 2;
    return repository.findAll().get(indexOfLastItem);
  }

  public int dayFrequency(
      school.hei.haapi.endpoint.rest.model.DelayPenalty.InterestTimerateEnum interestTimeRate) {
    switch (interestTimeRate) {
      case DAILY:
        return 1;
      default:
        throw new BadRequestException(
                "Unexpected delay Penalty Interest Time rate: " + interestTimeRate.getValue());
    }
  }
  public List<DelayPenaltyHistory> findDelayPenaltyHistoriesByInterestStartAndEnd(LocalDate InterestStart, LocalDate InterestEnd){
    List<DelayPenaltyHistory> repositoryDelayPenaltyHistories = repository.findDelayPenaltyHistoriesByInterestStartAndEnd(InterestStart,InterestEnd);

    if (repositoryDelayPenaltyHistories.size() == 0) {
      repositoryDelayPenaltyHistories = Arrays.asList(getLastItem());
    }

return removeUnusedDelayPenaltyHistories(repositoryDelayPenaltyHistories);
  }

  private List<DelayPenaltyHistory> removeUnusedDelayPenaltyHistories(List<DelayPenaltyHistory> delayPenaltyHistoryList){
    for (int i = 0; i < delayPenaltyHistoryList.size(); i++) {
      if (delayPenaltyHistoryList.get(i).getEndDate() != null) {
        if (delayPenaltyHistoryList.get(i).getStartDate().isEqual(delayPenaltyHistoryList.get(i).getEndDate())) {
          delayPenaltyHistoryList.remove(i);
        }
      } else {
          DelayPenaltyHistory end = delayPenaltyHistoryList.get(i);
          end.setEndDate(LocalDate.now(ZoneId.of("UTC")));
          delayPenaltyHistoryList.set(i, end);
      }
    }
      return delayPenaltyHistoryList;
    }

}
