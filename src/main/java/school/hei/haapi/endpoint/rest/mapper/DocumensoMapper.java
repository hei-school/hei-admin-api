package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DocumensoDocument;
import school.hei.haapi.endpoint.rest.model.DocumensoDocumentStatus;
import school.hei.haapi.endpoint.rest.model.TemplateDocumenso;

@Component
public class DocumensoMapper {
  public TemplateDocumenso toRest(school.hei.haapi.model.TemplateDocumenso domain) {
    return new TemplateDocumenso()
        .id(domain.getId())
        .documensoTemplateId(domain.getDocumensoTemplateId())
        .title(domain.getTitle())
        .type(domain.getType())
        .adminId(domain.getAdmin() == null ? null : domain.getAdmin().getId());
  }

  public DocumensoDocument toRest(school.hei.haapi.model.DocumensoDocument domain) {
    return new DocumensoDocument()
        .id(domain.getId())
        .documensoDocumentId(domain.getDocumensoDocumentId())
        .status(DocumensoDocumentStatus.valueOf(domain.getStatus().name()))
        .studentId(domain.getStudent().getId())
        .level(domain.getLevel())
        .templateId(domain.getTemplate().getId());
  }
}
