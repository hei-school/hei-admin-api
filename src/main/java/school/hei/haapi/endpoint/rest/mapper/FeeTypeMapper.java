package school.hei.haapi.endpoint.rest.mapper;

import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.FeeType;
import school.hei.haapi.endpoint.rest.model.FeeTypeComponent;
import school.hei.haapi.model.FeeTypeEntity;

@Component
@AllArgsConstructor
public class FeeTypeMapper {

  private final FeeTypeComponentMapper feeTypeComponentMapper;

  public FeeType toRest(FeeTypeEntity feeTypeEntity) {
    return new FeeType()
        .id(feeTypeEntity.getId())
        .types(
            feeTypeEntity.getFeeTypeComponentEntities().stream()
                .map(feeTypeComponentMapper::toRest)
                .collect(toList()));
  }

  public FeeType toRest(FeeTypeEntity feeTypeEntity, List<FeeTypeComponent> feeTypeComponents) {
    return new FeeType().id(feeTypeEntity.getId()).types(feeTypeComponents);
  }
}
