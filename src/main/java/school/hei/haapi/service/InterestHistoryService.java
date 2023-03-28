package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.InterestHistory;
import school.hei.haapi.repository.InterestHistoryRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class InterestHistoryService {

  private final InterestHistoryRepository repository;

  public InterestHistory getById(String interestHistoryId) {
    return repository.getById(interestHistoryId);
  }

  public List<InterestHistory> getAll() {
    return repository.findAll();
  }

  public List<InterestHistory> saveAll(List<InterestHistory> interestHistorys) {
    return repository.saveAll(interestHistorys);
  }

  public int getInterestAmount(){
    return 1000;
  }
}
