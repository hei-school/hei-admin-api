package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateFeeTemplate;
import school.hei.haapi.model.FeeTemplate;

@Component
@AllArgsConstructor
public class FeeTemplateMapper {

  private final FeeTemplateTypeEnumMapper feeTemplateTypeEnumMapper;

  public FeeTemplate toDomain(CrupdateFeeTemplate rest) {
    return FeeTemplate.builder()
        .id(rest.getId())
        .name(rest.getName())
        .amount(rest.getAmount())
        .numberOfPayments(rest.getNumberOfPayments())
        .type(feeTemplateTypeEnumMapper.toDomainEnum(rest.getType()))
        .build();
  }

  public school.hei.haapi.endpoint.rest.model.FeeTemplate toRest(FeeTemplate domain) {
    return new school.hei.haapi.endpoint.rest.model.FeeTemplate()
        .id(domain.getId())
        .type(domain.getType())
        .amount(domain.getAmount())
        .name(domain.getName())
        .creationDatetime(domain.getCreationDatetime())
        .numberOfPayments(domain.getNumberOfPayments());
  }
}
