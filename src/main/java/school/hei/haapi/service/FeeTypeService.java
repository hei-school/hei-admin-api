package school.hei.haapi.service;

import static java.util.stream.Collectors.toList;

import java.util.List;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.FeeType;
import school.hei.haapi.model.*;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.FeeTypeValidator;
import school.hei.haapi.repository.FeeTypeRepository;

@Service
@AllArgsConstructor
@Slf4j
public class FeeTypeService {
  private final FeeTypeRepository feeTypeRepository;
  private final FeeTypeComponentService feeTypeComponentService;
  private final FeeTypeValidator feeTypeValidator;

  public List<FeeTypeEntity> getAll() {
    return feeTypeRepository.findAll();
  }

  @Transactional
  public FeeTypeEntity updateOrSave(
      List<FeeTypeComponentEntity> feeTypeComponentEntities, FeeType feeType) {
    feeTypeValidator.accept(feeType);
    FeeTypeEntity saved =
        feeTypeRepository.save(FeeTypeEntity.builder().id(feeType.getId()).build());

    if (feeType.getId() != null) {
      List<FeeTypeComponentEntity> actualFeeTypeComponentEntities =
          feeTypeComponentService.getByFeeTypeId(feeType.getId());
      feeTypeComponentService.deleteAll(actualFeeTypeComponentEntities);
    }

    List<FeeTypeComponentEntity> newFeeTypeComponentEntities =
        feeTypeComponentService.updateOrSaveAll(
            feeTypeComponentEntities.stream()
                .map(
                    typeFeeComponent -> {
                      typeFeeComponent.setFeeTypeEntity(saved);
                      return typeFeeComponent;
                    })
                .collect(toList()));

    return findById(saved.getId());
  }

  public FeeTypeEntity findById(String id) {
    return feeTypeRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("typeFee with id = " + id + " not found"));
  }
}
