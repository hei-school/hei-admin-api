package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.FeeTypeComponentEntity;
import school.hei.haapi.repository.FeeTypeComponentRepository;

@Service
@AllArgsConstructor
@Slf4j
public class FeeTypeComponentService {
  private final FeeTypeComponentRepository feeTypeComponentRepository;

  public List<FeeTypeComponentEntity> getByFeeTypeId(String feeTypeId) {
    return feeTypeComponentRepository.getByFeeTypeEntityId(feeTypeId);
  }

  public List<FeeTypeComponentEntity> updateOrSaveAll(
      List<FeeTypeComponentEntity> feeTypeComponentEntities) {
    return feeTypeComponentRepository.saveAll(feeTypeComponentEntities);
  }

  public void deleteAll(List<FeeTypeComponentEntity> feeTypeComponentEntities) {
    feeTypeComponentRepository.deleteAll(feeTypeComponentEntities);
  }
}
