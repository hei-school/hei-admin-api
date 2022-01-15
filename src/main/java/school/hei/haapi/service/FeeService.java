package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Fee;
import school.hei.haapi.repository.FeeRepository;

@Service
@AllArgsConstructor
public class FeeService {

  private final FeeRepository feeRepository;

  public Fee getByUserIdAndId(String userId, String feeId) {
    return feeRepository.getByUserIdAndId(userId, feeId);
  }
}
