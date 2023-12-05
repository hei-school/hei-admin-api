package school.hei.haapi.service;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.*;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.FeeTypeComponentValidator;
import school.hei.haapi.model.validator.FeeTypeValidator;
import school.hei.haapi.model.validator.UUIDValidator;
import school.hei.haapi.repository.FeeTypeRepository;

@Service
@AllArgsConstructor
public class FeeTypeService {
  private final FeeTypeRepository feeTypeRepository;
  private final FeeTypeComponentService feeTypeComponentService;
  private final FeeTypeValidator feeTypeValidator;
  private final FeeTypeComponentValidator feeTypeComponentValidator;
  private final UUIDValidator uuidValidator;

  public List<FeeTypeEntity> getAll() {
    return feeTypeRepository.findAll();
  }

  @Transactional
  public FeeTypeEntity updateOrSave(FeeTypeEntity feeType) {

    feeTypeValidator.accept(feeType);

    List<FeeTypeComponentEntity> feeTypeComponentEntities = feeType.getFeeTypeComponentEntities();
    if (feeTypeRepository.findById(Objects.requireNonNull(feeType.getId())).isPresent()) {
      List<FeeTypeComponentEntity> actualFeeTypeComponentEntities =
          feeTypeComponentService.getByFeeTypeId(feeType.getId());
      feeTypeComponentService.deleteAll(actualFeeTypeComponentEntities);
    } else {
      uuidValidator.accept(feeType.getId());
    }

    FeeTypeEntity saved =
        feeTypeRepository.save(
            FeeTypeEntity.builder().id(feeType.getId()).name(feeType.getName()).build());

    feeTypeComponentService.updateOrSaveAll(
        feeTypeComponentEntities.stream()
            .map(
                typeFeeComponent -> {
                  typeFeeComponent.setFeeTypeEntity(saved);
                  return typeFeeComponent;
                })
            .collect(toList()));

    List<FeeTypeComponentEntity> feeComponents =
        feeTypeComponentService.getByFeeTypeId(feeType.getId());
    return findById(saved.getId()).toBuilder().feeTypeComponentEntities(feeComponents).build();
  }

  public FeeTypeEntity findById(String id) {
    return feeTypeRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("typeFee with id = " + id + " not found"));
  }
}
