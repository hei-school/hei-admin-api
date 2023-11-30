package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.FeeTypeComponentMapper;
import school.hei.haapi.endpoint.rest.mapper.FeeTypeMapper;
import school.hei.haapi.endpoint.rest.model.FeeType;
import school.hei.haapi.endpoint.rest.model.FeeTypeComponent;
import school.hei.haapi.model.FeeTypeComponentEntity;
import school.hei.haapi.model.FeeTypeEntity;
import school.hei.haapi.service.FeeTypeComponentService;
import school.hei.haapi.service.FeeTypeService;

@RestController
@AllArgsConstructor
public class FeeTypeController {
  private final FeeTypeService feeTypeService;
  private final FeeTypeMapper feeTypeMapper;
  private final FeeTypeComponentMapper feeTypeComponentMapper;
  private final FeeTypeComponentService feeTypeComponentService;

  @PutMapping("/fee_type")
  public FeeType createOrUpdateFeeType(@RequestBody FeeType feeType) {
    List<FeeTypeComponentEntity> feeTypeComponentsToAddEntity =
        feeTypeComponentMapper.toDomain(feeType);
    FeeTypeEntity feeTypeEntity =
        feeTypeService.updateOrSave(feeTypeComponentsToAddEntity, feeType);
    List<FeeTypeComponent> feeComponents =
        feeTypeComponentService.getByFeeTypeId(feeTypeEntity.getId()).stream()
            .map(feeTypeComponentMapper::toRest)
            .collect(toList());

    return feeTypeMapper.toRest(feeTypeEntity, feeComponents);
  }

  @GetMapping("/fee_type")
  public List<FeeType> getAllFeeType() {
    return feeTypeService.getAll().stream().map(feeTypeMapper::toRest).collect(toList());
  }
}
