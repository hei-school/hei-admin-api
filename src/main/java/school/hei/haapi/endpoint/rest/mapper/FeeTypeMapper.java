package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateFeeType;
import school.hei.haapi.endpoint.rest.model.FeeType;

@Component
public class FeeTypeMapper {

  public FeeType toRest(school.hei.haapi.model.FeeType domain) {
    return new FeeType()
        .id(domain.getId())
        .name(domain.getName())
        .creationDatetime(domain.getCreationDatetime())
        .totalAmount(domain.getTotalAmount())
        .numberOfMonths(domain.getNumberOfMonths());
  }

  public school.hei.haapi.model.FeeType toDomain(CrupdateFeeType rest) {
    return school.hei.haapi.model.FeeType.builder()
        .id(rest.getId())
        .name(rest.getName())
        .totalAmount(rest.getTotalAmount())
        .numberOfMonths(rest.getNumberOfMonths())
        .build();
  }
}
