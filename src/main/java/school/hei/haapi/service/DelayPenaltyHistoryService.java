package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.DelayPenaltyHistory;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.DelayPenaltyHistoryRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class DelayPenaltyHistoryService {
  private final DelayPenaltyHistoryRepository repository;
  private final DelayPenaltyHistoryRepository delayPenaltyHistoryRepository;

  public DelayPenaltyHistory getById(String delayHistory) {
    return repository.getById(delayHistory);
  }

  public DelayPenaltyHistory save(DelayPenaltyHistory delayPenaltyHistory) {
    return repository.save(delayPenaltyHistory);
  }

  public DelayPenaltyHistory toBeSaved(DelayPenalty delayPenalty) {
    DelayPenaltyHistory previousDelayPenalty = getBeforeLastItem();
    DelayPenaltyHistory lastDelayPenalty = getLastItem();
    return DelayPenaltyHistory.builder()
        .delayPenalty(delayPenalty)
        .interestPercent(delayPenalty.getInterestPercent())
        .timeFrequency(dayFrequency(Objects.requireNonNull(delayPenalty.getInterestTimeRate())))
        .startDate(previousDelayPenalty.getEndDate())
        .endDate(null)
        .creationDate(Instant.now()).build();
  }

  public DelayPenaltyHistory toSavePrevious(DelayPenalty delayPenalty) {
    DelayPenaltyHistory previousDelayPenalty = getBeforeLastItem();
    DelayPenaltyHistory lastDelayPenalty = getLastItem();
    Instant instant = Instant.now();
    ZoneId zoneId = ZoneId.systemDefault();
    ZonedDateTime zonedDateTime = instant.atZone(zoneId);
    LocalDate localDate = zonedDateTime.toLocalDate();
    return DelayPenaltyHistory.builder()
        .id(lastDelayPenalty.getId())
        .delayPenalty(delayPenalty)
        .interestPercent(lastDelayPenalty.getInterestPercent())
        .timeFrequency(dayFrequency(Objects.requireNonNull(delayPenalty.getInterestTimeRate())))
        .startDate(lastDelayPenalty.getStartDate())
        .endDate(localDate)
        .creationDate(lastDelayPenalty.getCreationDate()).build();
  }

  private DelayPenaltyHistory getLastItem() {
    int indexOfLastItem = delayPenaltyHistoryRepository.findAll().size() - 1;
    return delayPenaltyHistoryRepository.findAll().get(indexOfLastItem);
  }

  private DelayPenaltyHistory getFirstItem() {
    return delayPenaltyHistoryRepository.findAll().get(0);
  }

  private DelayPenaltyHistory getBeforeLastItem() {
    int indexOfLastItem = delayPenaltyHistoryRepository.findAll().size() - 2;
    return delayPenaltyHistoryRepository.findAll().get(indexOfLastItem);
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
      repositoryDelayPenaltyHistories = Arrays.asList(getFirstItem());
    }

return removeUnusedDelayPenaltyHistories(repositoryDelayPenaltyHistories);
  }

  public List<DelayPenaltyHistory> removeUnusedDelayPenaltyHistories(List<DelayPenaltyHistory> delayPenaltyHistoryList) {
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
