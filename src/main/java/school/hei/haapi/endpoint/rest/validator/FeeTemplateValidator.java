package school.hei.haapi.endpoint.rest.validator;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.FeeTemplateContent;
import school.hei.haapi.endpoint.rest.model.V2CrupdateFeeTemplate;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class FeeTemplateValidator {

  public void accept(List<V2CrupdateFeeTemplate> feeTemplates) {
    if (feeTemplates == null) {
      throw new BadRequestException("Provided fee templates list is null");
    }
    feeTemplates.forEach(this::accept);
  }

  public void accept(V2CrupdateFeeTemplate feeTemplate) {
    if (feeTemplate == null) {
      throw new BadRequestException("Provided fee template is null");
    }
    if (feeTemplate.getId() == null) {
      throw new BadRequestException("Fee template id is mandatory");
    }
    if (feeTemplate.getLabel() == null) {
      throw new BadRequestException("Fee template label is mandatory");
    }
    if (feeTemplate.getType() == null) {
      throw new BadRequestException("Fee template type is mandatory");
    }
    if (feeTemplate.getCategory() == null) {
      throw new BadRequestException("Fee template category is mandatory");
    }
  }

  public void acceptContents(List<FeeTemplateContent> feeTemplateContents) {
    if (feeTemplateContents == null) {
      throw new BadRequestException("Provided fee template contents list is null");
    }
    feeTemplateContents.forEach(this::acceptContent);
  }

  public void acceptContent(FeeTemplateContent feeTemplateContent) {
    if (feeTemplateContent == null) {
      throw new BadRequestException("Provided fee template content is null");
    }
    if (feeTemplateContent.getId() == null) {
      throw new BadRequestException("Fee template content id is mandatory");
    }
    if (feeTemplateContent.getLabel() == null) {
      throw new BadRequestException("Fee template content label is mandatory");
    }
    if (feeTemplateContent.getAmount() == null) {
      throw new BadRequestException("Fee template content amount is mandatory");
    }
    if (feeTemplateContent.getAmount() <= 0) {
      throw new BadRequestException("Fee template content amount must be greater than 0");
    }
    if (feeTemplateContent.getDueDate() == null) {
      throw new BadRequestException("Fee template content due date is mandatory");
    }
  }
}
