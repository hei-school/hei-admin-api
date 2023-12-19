package school.hei.haapi.endpoint.rest.mapper;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.FeeTypeComponent;
import school.hei.haapi.model.FeeTypeComponentEntity;
import school.hei.haapi.model.FeeTypeEntity;
import school.hei.haapi.model.validator.FeeTypeValidator;

@Component
@AllArgsConstructor
public class FeeTypeComponentMapper {
  private final FeeTypeValidator feeTypeValidator;

  public FeeTypeComponent toRest(FeeTypeComponentEntity feeTypeComponentEntity) {
    return new FeeTypeComponent()
        .name(feeTypeComponentEntity.getName())
        .monthsNumber(feeTypeComponentEntity.getMonthsNumber())
        .totalAmount(feeTypeComponentEntity.getTotalAmount())
        .type(feeTypeComponentEntity.getType());
  }

  public FeeTypeComponentEntity toDomain(
      FeeTypeComponent feeTypeComponent, FeeTypeEntity feeTypeEntity) {
    return FeeTypeComponentEntity.builder()
        .feeTypeEntity(feeTypeEntity)
        .name(feeTypeComponent.getName())
        .monthsNumber(feeTypeComponent.getMonthsNumber())
        .totalAmount(feeTypeComponent.getTotalAmount())
        .type(feeTypeComponent.getType())
        .build();
  }

  public FeeTypeComponentEntity toDomain(FeeTypeComponent feeTypeComponent) {
    return FeeTypeComponentEntity.builder()
        .name(feeTypeComponent.getName())
        .monthsNumber(feeTypeComponent.getMonthsNumber())
        .totalAmount(feeTypeComponent.getTotalAmount())
        .type(feeTypeComponent.getType())
        .build();
  }
}
