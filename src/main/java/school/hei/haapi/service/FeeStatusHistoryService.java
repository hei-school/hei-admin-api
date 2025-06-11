package school.hei.haapi.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeStatusHistory;
import school.hei.haapi.repository.FeeStatusHistoryRepository;

@Service
@RequiredArgsConstructor
public class FeeStatusHistoryService {
  private final FeeStatusHistoryRepository repository;

  public FeeStatusHistory saveFeeStatus(FeeStatusEnum status, Fee fee) {
    return repository.save(FeeStatusHistory.builder().fee(fee).status(status).build());
  }

  public List<FeeStatusHistory> getFeeStatusHistory(String feeId, Sort sort) {
    return repository.findByFeeIdOrderByDatetime(feeId, sort);
  }
}
