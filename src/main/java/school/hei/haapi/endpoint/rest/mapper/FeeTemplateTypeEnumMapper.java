package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateFeeTemplate;
import school.hei.haapi.endpoint.rest.model.FeeTemplate;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class FeeTemplateTypeEnumMapper {

  public CrupdateFeeTemplate.TypeEnum toRestEnum(FeeTemplate.TypeEnum domain) {
    if (domain == null) {
      return null;
    }
    return switch (domain) {
      case TUITION -> CrupdateFeeTemplate.TypeEnum.TUITION;
      case HARDWARE -> CrupdateFeeTemplate.TypeEnum.HARDWARE;
      case STUDENT_INSURANCE -> CrupdateFeeTemplate.TypeEnum.STUDENT_INSURANCE;
      case REMEDIAL_COSTS -> CrupdateFeeTemplate.TypeEnum.REMEDIAL_COSTS;
      default -> throw new BadRequestException("Unexpected type " + domain);
    };
  }

  public FeeTemplate.TypeEnum toDomainEnum(CrupdateFeeTemplate.TypeEnum rest) {
    if (rest == null) {
      return null;
    }
    return switch (rest) {
      case TUITION -> FeeTemplate.TypeEnum.TUITION;
      case HARDWARE -> FeeTemplate.TypeEnum.HARDWARE;
      case STUDENT_INSURANCE -> FeeTemplate.TypeEnum.STUDENT_INSURANCE;
      case REMEDIAL_COSTS -> FeeTemplate.TypeEnum.REMEDIAL_COSTS;
      default -> throw new BadRequestException("Unexpected type " + rest);
    };
  }
}
