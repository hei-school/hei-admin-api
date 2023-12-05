package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.FeeTypeComponentMapper;
import school.hei.haapi.endpoint.rest.mapper.FeeTypeMapper;
import school.hei.haapi.endpoint.rest.model.FeeType;
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

  @PutMapping("/fee_types/{fee_type_id}")
  public FeeType createOrUpdateFeeType(
      @PathVariable String fee_type_id, @RequestBody FeeType feeType) {
    FeeTypeEntity feeTypeEntityToSave = feeTypeMapper.toDomain(feeType, fee_type_id);
    FeeTypeEntity feeTypeEntity = feeTypeService.updateOrSave(feeTypeEntityToSave);

    return feeTypeMapper.toRest(feeTypeEntity);
  }

  @GetMapping("/fee_types")
  public List<FeeType> getAllFeeType() {
    return feeTypeService.getAll().stream()
        .map(feeTypeMapper::toRest)
        .collect(toUnmodifiableList());
  }
}
