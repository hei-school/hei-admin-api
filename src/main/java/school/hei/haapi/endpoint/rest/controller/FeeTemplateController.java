package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.FeeTemplateMapper;
import school.hei.haapi.endpoint.rest.model.FeeTemplateContent;
import school.hei.haapi.endpoint.rest.model.V2CrupdateFeeTemplate;
import school.hei.haapi.endpoint.rest.model.V2FeeTemplate;
import school.hei.haapi.endpoint.rest.validator.FeeTemplateValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.FeeTemplateService;

@RestController
@RequiredArgsConstructor
public class FeeTemplateController {
  private final FeeTemplateService feeTemplateService;
  private final FeeTemplateMapper feeTemplateMapper;
  private final FeeTemplateValidator feeTemplateValidator;

  @GetMapping("/feeTemplates")
  public List<V2FeeTemplate> getV2FeeTemplates(
      @RequestParam PageFromOne page, @RequestParam BoundedPageSize pageSize) {
    var v2FeeTemplates = feeTemplateService.getV2FeeTemplates(page.getValue(), pageSize.getValue());
    return v2FeeTemplates.stream().map(feeTemplateMapper::toRest).toList();
  }

  @PutMapping("/feeTemplates")
  public List<V2FeeTemplate> crupdateV2FeeTemplates(
      @RequestBody List<V2CrupdateFeeTemplate> v2CrupdateFeeTemplates) {
    feeTemplateValidator.accept(v2CrupdateFeeTemplates);
    var v2FeeTemplates = v2CrupdateFeeTemplates.stream().map(feeTemplateMapper::toDomain).toList();
    return feeTemplateService.crupdateV2FeeTemplates(v2FeeTemplates).stream()
        .map(feeTemplateMapper::toRest)
        .toList();
  }

  @GetMapping("/feeTemplates/{id}/content")
  public List<FeeTemplateContent> getContentByFeeTemplateId(
      @PathVariable(name = "id") String feeTemplateIdentifier) {
    return feeTemplateService.getFeeTemplateContentsByTemplateId(feeTemplateIdentifier).stream()
        .map(feeTemplateMapper::toRestContent)
        .toList();
  }

  @PutMapping("/feeTemplates/{id}/content")
  public List<FeeTemplateContent> crupdateFeeTemplatesContent(
      @PathVariable(name = "id") String feeTemplateIdentifier,
      @RequestBody List<FeeTemplateContent> feeTemplateContents) {
    feeTemplateValidator.acceptContents(feeTemplateContents);
    var v2FeeTemplateContents =
        feeTemplateContents.stream().map(feeTemplateMapper::toDomainContent).toList();
    return feeTemplateService
        .crupdateV2FeeTemplateContents(feeTemplateIdentifier, v2FeeTemplateContents)
        .stream()
        .map(feeTemplateMapper::toRestContent)
        .toList();
  }
}
