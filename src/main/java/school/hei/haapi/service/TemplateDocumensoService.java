package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.TemplateDocumenso;
import school.hei.haapi.repository.TemplateDocumensoRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.gen.model.TemplateFindTemplates200ResponseDataInner;

@Service
@AllArgsConstructor
public class TemplateDocumensoService {
  private final DocumensoClient documensoClient;
  private final TemplateDocumensoRepository templateDocumensoRepository;
  private final UserRepository userRepository;

  @SneakyThrows
  public List<TemplateDocumenso> syncTemplates() {
    var response = documensoClient.findTemplates(null, 1, 100);
    return response.getData().stream().map(this::upsert).toList();
  }

  private TemplateDocumenso upsert(TemplateFindTemplates200ResponseDataInner remote) {
    var documensoTemplateId = remote.getId().longValue();
    var template =
        templateDocumensoRepository
            .findByDocumensoTemplateId(documensoTemplateId)
            .orElseGet(TemplateDocumenso::new);
    template.setDocumensoTemplateId(documensoTemplateId);
    template.setTitle(remote.getTitle());
    template.setType(remote.getType() == null ? null : remote.getType().getValue());
    if (remote.getUserId() != null) {
      userRepository
          .findByDocumensoUserId(remote.getUserId().longValue())
          .ifPresent(template::setAdmin);
    }
    return templateDocumensoRepository.save(template);
  }
}
