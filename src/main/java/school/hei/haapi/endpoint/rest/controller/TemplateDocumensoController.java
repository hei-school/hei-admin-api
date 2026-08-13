package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DocumensoMapper;
import school.hei.haapi.endpoint.rest.model.TemplateDocumenso;
import school.hei.haapi.service.TemplateDocumensoService;

@RestController
@RequiredArgsConstructor
public class TemplateDocumensoController {
  private final TemplateDocumensoService templateDocumensoService;
  private final DocumensoMapper documensoMapper;

  @PostMapping("/documenso-templates/sync")
  public List<TemplateDocumenso> syncDocumensoTemplates() {
    return templateDocumensoService.syncTemplates().stream().map(documensoMapper::toRest).toList();
  }
}
