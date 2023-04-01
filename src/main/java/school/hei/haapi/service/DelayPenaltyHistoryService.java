package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.DelayPenaltyHistory;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.DelayPenaltyHistoryValidator;
import school.hei.haapi.repository.DelayPenaltyHistoryRepository;
import school.hei.haapi.service.utils.DataFormatterUtils;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class DelayPenaltyHistoryService {
  private final DelayPenaltyHistoryRepository repository;
  private  final DelayPenaltyHistoryValidator delayPenaltyHistoryValidator;

  public DelayPenaltyHistory getById(String delayHistory) {
    return repository.getById(delayHistory);
  }

  public DelayPenaltyHistory save(DelayPenaltyHistory delayPenaltyHistory) {
    delayPenaltyHistoryValidator.accept(delayPenaltyHistory);
    return repository.save(delayPenaltyHistory);
  }

  public List<DelayPenaltyHistory> saveAll(List<DelayPenaltyHistory> delayPenaltyHistories) {
    delayPenaltyHistoryValidator.accept(delayPenaltyHistories);
    return repository.saveAll(delayPenaltyHistories);
  }

  @Transactional
  public void updateWhenUpdatedDelayPenalty(DelayPenalty lastDelayPenalty, DelayPenalty newDelayPenalty) {
    LocalDate DebutOfApplicationOfConfGen = LocalDate.of(2020,1,1);
    DelayPenaltyHistory newDelayPenaltyHistory = DelayPenaltyHistory.builder()
            .delayPenalty(newDelayPenalty)
            .interestPercent(newDelayPenalty.getInterestPercent())
            .timeFrequency(dayFrequency(newDelayPenalty.getInterestTimeRate()))
            .startDate(DebutOfApplicationOfConfGen)
            .endDate(null)
            .creationDate(Instant.now()).build();
    delayPenaltyHistoryValidator.accept(newDelayPenaltyHistory);
    DelayPenaltyHistory lastDelayPenaltyHistoryToModify = getLastItem();
    if (lastDelayPenaltyHistoryToModify!=null){
      newDelayPenaltyHistory.setStartDate(DataFormatterUtils.takeLocalDate());
      lastDelayPenaltyHistoryToModify.setInterestPercent(lastDelayPenalty.getInterestPercent());
      lastDelayPenaltyHistoryToModify.setTimeFrequency(dayFrequency(lastDelayPenalty.getInterestTimeRate()));
      lastDelayPenaltyHistoryToModify.setEndDate(DataFormatterUtils.takeLocalDate());
      if (!lastDelayPenaltyHistoryToModify.getStartDate().isEqual(DebutOfApplicationOfConfGen)){
        lastDelayPenaltyHistoryToModify.setStartDate(LocalDate.ofInstant(lastDelayPenalty.getLastUpdateDate(),ZoneId.of("UTC")));
      }
      delayPenaltyHistoryValidator.accept(lastDelayPenaltyHistoryToModify);
      repository.saveAll(List.of(lastDelayPenaltyHistoryToModify,newDelayPenaltyHistory));
    }else {
      repository.save(newDelayPenaltyHistory);
    }
  }

  private DelayPenaltyHistory getLastItem() {

    return repository.findAll(Sort.by(Sort.Direction.DESC, "creationDate")).size()>0?
            repository.findAll(Sort.by(Sort.Direction.DESC, "creationDate")).get(0)
            : null;
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
  public List<DelayPenaltyHistory> findDelayPenaltyHistoriesByInterestStartAndEnd(LocalDate interestStart, LocalDate interestEnd){
    List<DelayPenaltyHistory> repositoryDelayPenaltyHistories = repository.findDelayPenaltyHistoriesByInterestStartAndEnd(interestStart,interestEnd);

    if (repositoryDelayPenaltyHistories.size() == 0 && !interestStart.isAfter(repository.findAll().get(0).getEndDate())) {

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
