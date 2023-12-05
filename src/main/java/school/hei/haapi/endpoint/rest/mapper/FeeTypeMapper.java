package school.hei.haapi.endpoint.rest.mapper;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.FeeType;
import school.hei.haapi.endpoint.rest.model.FeeTypeComponent;
import school.hei.haapi.model.FeeTypeComponentEntity;
import school.hei.haapi.model.FeeTypeEntity;

@Component
@AllArgsConstructor
public class FeeTypeMapper {

  private final FeeTypeComponentMapper feeTypeComponentMapper;

  public FeeType toRest(FeeTypeEntity feeTypeEntity) {
    return new FeeType()
        .id(feeTypeEntity.getId())
        .name(feeTypeEntity.getName())
        .types(
            feeTypeEntity.getFeeTypeComponentEntities().stream()
                .map(feeTypeComponentMapper::toRest)
                .collect(toUnmodifiableList()));
  }

  public FeeType toRest(FeeTypeEntity feeTypeEntity, List<FeeTypeComponent> feeTypeComponents) {
    return new FeeType()
        .id(feeTypeEntity.getId())
        .name(feeTypeEntity.getName())
        .types(feeTypeComponents);
  }

  public FeeTypeEntity toDomain(FeeType feeType, String feeTypeId) {
    List<FeeTypeComponentEntity> feeTypeComponents =
        Objects.requireNonNull(feeType.getTypes()).stream()
            .map(feeTypeComponentMapper::toDomain)
            .toList();
    return FeeTypeEntity.builder()
        .id(feeTypeId)
        .name(feeType.getName())
        .feeTypeComponentEntities(feeTypeComponents)
        .build();
  }
}
