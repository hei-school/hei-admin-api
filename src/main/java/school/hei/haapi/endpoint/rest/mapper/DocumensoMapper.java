package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.DocumensoDocument;
import school.hei.haapi.endpoint.rest.model.DocumensoDocumentStatus;
import school.hei.haapi.endpoint.rest.model.TemplateDocumenso;

@Component
@AllArgsConstructor
public class DocumensoMapper {
  private final UserMapper userMapper;

  public school.hei.haapi.model.DocumensoDocumentStatus toDomain(DocumensoDocumentStatus rest) {
    return rest == null
        ? null
        : school.hei.haapi.model.DocumensoDocumentStatus.valueOf(rest.name());
  }

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
        .subjectId(domain.getSubject().getId())
        .subject(userMapper.toIdentifier(domain.getSubject()))
        .level(domain.getLevel())
        .templateId(domain.getTemplate().getId())
        .templateTitle(domain.getTemplate().getTitle())
        .completedDatetime(domain.getCompletedDatetime())
        .generatedById(domain.getGeneratedBy() == null ? null : domain.getGeneratedBy().getId());
  }
}
