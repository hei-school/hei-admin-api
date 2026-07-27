package school.hei.haapi.endpoint.rest.mapper;

import java.math.BigInteger;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.model.V2FeeTemplateContent;

@Component
@AllArgsConstructor
public class FeeTemplateMapper {

  public school.hei.haapi.model.FeeTemplate toDomain(CrupdateFeeTemplate rest) {
    return school.hei.haapi.model.FeeTemplate.builder()
        .id(rest.getId())
        .name(rest.getName())
        .amount(rest.getAmount())
        .category(rest.getCategory())
        .frequency(rest.getFrequency())
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
        .category(domain.getCategory())
        .frequency(domain.getFrequency())
        .creationDatetime(domain.getCreationDatetime())
        .numberOfPayments(domain.getNumberOfPayments());
  }

  public V2FeeTemplate toRest(school.hei.haapi.model.V2FeeTemplate domain) {
    return new V2FeeTemplate()
        .id(domain.getId())
        .label(domain.getLabel())
        .type(domain.getType())
        .category(domain.getCategory())
        .creationDatetime(domain.getCreationDatetime());
  }

  public V2DetailedFeeTemplate toRestDetailed(school.hei.haapi.model.V2FeeTemplate domain) {
    var contents = domain.getFeeTemplateContents();
    return new V2DetailedFeeTemplate()
        .id(domain.getId())
        .label(domain.getLabel())
        .type(domain.getType())
        .category(domain.getCategory())
        .creationDatetime(domain.getCreationDatetime())
        .contents(
            contents == null ? List.of() : contents.stream().map(this::toRestContent).toList());
  }

  public school.hei.haapi.model.V2FeeTemplate toDomain(V2CrupdateFeeTemplate rest) {
    return school.hei.haapi.model.V2FeeTemplate.builder()
        .id(rest.getId())
        .label(rest.getLabel())
        .type(rest.getType())
        .category(rest.getCategory())
        .build();
  }

  public FeeTemplateContent toRestContent(school.hei.haapi.model.V2FeeTemplateContent domain) {
    return new FeeTemplateContent()
        .id(domain.getId())
        .label(domain.getLabel())
        .amount(domain.getAmount().intValue())
        .dueDate(domain.getDueDate());
  }

  public school.hei.haapi.model.V2FeeTemplateContent toDomainContent(FeeTemplateContent rest) {
    return V2FeeTemplateContent.builder()
        .id(rest.getId())
        .label(rest.getLabel())
        .amount(rest.getAmount() == null ? null : BigInteger.valueOf(rest.getAmount()))
        .dueDate(rest.getDueDate())
        .build();
  }
}
