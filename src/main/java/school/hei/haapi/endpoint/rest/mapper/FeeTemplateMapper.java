package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateFeeTemplate;
import school.hei.haapi.endpoint.rest.model.FeeTemplate;


@Component
@AllArgsConstructor
public class FeeTemplateMapper {

  public school.hei.haapi.model.FeeTemplate toDomain(CrupdateFeeTemplate rest) {
    return school.hei.haapi.model.FeeTemplate.builder()
        .id(rest.getId())
        .name(rest.getName())
        .amount(rest.getAmount())
        .numberOfPayments(rest.getNumberOfPayments())
        .type(rest.getType())
        .build();
  }

  public FeeTemplate toRest(school.hei.haapi.model.FeeTemplate domain) {
    return new school.hei.haapi.endpoint.rest.model.FeeTemplate()
        .id(domain.getId())
        .type(domain.getType())
        .amount(domain.getAmount())
        .name(domain.getName())
        .creationDatetime(domain.getCreationDatetime())
        .numberOfPayments(domain.getNumberOfPayments());
  }
}
