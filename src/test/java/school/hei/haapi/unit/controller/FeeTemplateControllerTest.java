package school.hei.haapi.unit.controller;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.controller.FeeTemplateController;
import school.hei.haapi.endpoint.rest.mapper.FeeTemplateMapper;
import school.hei.haapi.endpoint.rest.model.FeeTemplateContent;
import school.hei.haapi.endpoint.rest.model.V2CrupdateFeeTemplate;
import school.hei.haapi.endpoint.rest.model.V2FeeTemplate;
import school.hei.haapi.endpoint.rest.validator.FeeTemplateValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.V2FeeTemplateContent;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.FeeTemplateService;

class FeeTemplateControllerTest {
  private FeeTemplateService feeTemplateService;
  private FeeTemplateMapper feeTemplateMapper;
  private FeeTemplateValidator feeTemplateValidator;
  private FeeTemplateController subject;

  @BeforeEach
  void setUp() {
    feeTemplateService = mock();
    feeTemplateMapper = mock();
    feeTemplateValidator = mock();
    subject =
        new FeeTemplateController(feeTemplateService, feeTemplateMapper, feeTemplateValidator);
  }

  @Test
  void getV2FeeTemplates_delegates_and_maps_to_rest() {
    var restDomain = new school.hei.haapi.model.V2FeeTemplate();
    var rest = new V2FeeTemplate().id(randomUUID().toString());
    when(feeTemplateService.getV2FeeTemplates(1, 10)).thenReturn(List.of(restDomain));
    when(feeTemplateMapper.toRest(restDomain)).thenReturn(rest);

    var actual = subject.getV2FeeTemplates(new PageFromOne(1), new BoundedPageSize(10));

    assertEquals(List.of(rest), actual);
    verify(feeTemplateService).getV2FeeTemplates(1, 10);
  }

  @Test
  void crupdateV2FeeTemplates_validates_maps_and_delegates() {
    var payload = new V2CrupdateFeeTemplate().id(randomUUID().toString());
    var domain = new school.hei.haapi.model.V2FeeTemplate();
    var savedDomain = new school.hei.haapi.model.V2FeeTemplate();
    var rest = new V2FeeTemplate().id(randomUUID().toString());
    when(feeTemplateMapper.toDomain(payload)).thenReturn(domain);
    when(feeTemplateService.crupdateV2FeeTemplates(List.of(domain)))
        .thenReturn(List.of(savedDomain));
    when(feeTemplateMapper.toRest(savedDomain)).thenReturn(rest);

    var actual = subject.crupdateV2FeeTemplates(List.of(payload));

    assertEquals(List.of(rest), actual);
    verify(feeTemplateValidator).accept(List.of(payload));
    verify(feeTemplateService).crupdateV2FeeTemplates(List.of(domain));
  }

  @Test
  void crupdateV2FeeTemplates_does_not_persist_when_validation_fails() {
    var payload = new V2CrupdateFeeTemplate().id(randomUUID().toString());
    doThrow(new BadRequestException("Fee template label is mandatory"))
        .when(feeTemplateValidator)
        .accept(List.of(payload));

    assertThrows(BadRequestException.class, () -> subject.crupdateV2FeeTemplates(List.of(payload)));
    verify(feeTemplateService, never()).crupdateV2FeeTemplates(any());
  }

  @Test
  void getContentByFeeTemplateId_delegates_and_maps_to_rest_content() {
    var feeTemplateContentIdentifier = randomUUID().toString();
    var feeTemplateIdentifier = randomUUID().toString();
    var domainContent = V2FeeTemplateContent.builder().id(feeTemplateContentIdentifier).build();
    var restContent = new FeeTemplateContent().id(feeTemplateContentIdentifier);
    when(feeTemplateService.getFeeTemplateContentsByTemplateId(feeTemplateIdentifier))
        .thenReturn(List.of(domainContent));
    when(feeTemplateMapper.toRestContent(domainContent)).thenReturn(restContent);

    var actual = subject.getContentByFeeTemplateId(feeTemplateIdentifier);

    assertEquals(List.of(restContent), actual);
  }

  @Test
  void crupdateFeeTemplatesContent_validates_maps_and_delegates() {
    var feeTemplateContentIdentifier = randomUUID().toString();
    var feeTemplateIdentifier = randomUUID().toString();
    var payload = new FeeTemplateContent().id(feeTemplateContentIdentifier);
    var domainContent = V2FeeTemplateContent.builder().id(feeTemplateContentIdentifier).build();
    var savedContent = V2FeeTemplateContent.builder().id(feeTemplateContentIdentifier).build();
    var rest = new FeeTemplateContent().id(feeTemplateContentIdentifier);
    when(feeTemplateMapper.toDomainContent(payload)).thenReturn(domainContent);
    when(feeTemplateService.crupdateV2FeeTemplateContents(
            feeTemplateIdentifier, List.of(domainContent)))
        .thenReturn(List.of(savedContent));
    when(feeTemplateMapper.toRestContent(savedContent)).thenReturn(rest);

    var actual = subject.crupdateFeeTemplatesContent(feeTemplateIdentifier, List.of(payload));

    assertEquals(List.of(rest.id(feeTemplateIdentifier)), actual);
    verify(feeTemplateValidator).acceptContents(List.of(payload));
    verify(feeTemplateService)
        .crupdateV2FeeTemplateContents(feeTemplateIdentifier, List.of(domainContent));
  }

  @Test
  void crupdateFeeTemplatesContent_does_not_persist_when_validation_fails() {
    var payload = new FeeTemplateContent().id(randomUUID().toString());
    doThrow(new BadRequestException("Fee template content amount is mandatory"))
        .when(feeTemplateValidator)
        .acceptContents(List.of(payload));

    assertThrows(
        BadRequestException.class,
        () -> subject.crupdateFeeTemplatesContent(randomUUID().toString(), List.of(payload)));
    verify(feeTemplateService, never()).crupdateV2FeeTemplateContents(any(), any());
  }
}
