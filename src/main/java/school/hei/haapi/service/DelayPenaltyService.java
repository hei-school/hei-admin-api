package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.validator.DelayPenaltyValidator;
import school.hei.haapi.repository.DelayPenaltyRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class DelayPenaltyService {
  private final DelayPenaltyRepository repository;

  private final DelayPenaltyValidator delayPenaltyValidator;

  private final DelayPenaltyHistoryService delayPenaltyHistoryService;
  private final FeeService feeService;

  public DelayPenalty getById(String delayId) {
    return repository.getById(delayId);
  }

  public List<DelayPenalty> getAll() {
    return repository.findAll();
  }

  public DelayPenalty save(DelayPenalty delayPenalties) {
    delayPenaltyValidator.accept(delayPenalties);
    return repository.save(delayPenalties);
  }

  public DelayPenalty updateDelayPenalty(DelayPenalty newDelayPenalty) {
    delayPenaltyValidator.accept(newDelayPenalty);
    DelayPenalty lastDelayPenalty = getLastUpdated();
    delayPenaltyHistoryService.updateWhenUpdatedDelayPenalty(lastDelayPenalty,newDelayPenalty);
    repository.save(newDelayPenalty);
    feeService.updateFeesInterest();
    return newDelayPenalty;
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