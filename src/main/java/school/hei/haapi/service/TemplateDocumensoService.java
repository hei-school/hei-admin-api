package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.TemplateDocumenso;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.repository.TemplateDocumensoRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.gen.model.TemplateFindTemplates200ResponseDataInner;

@Slf4j
@Service
@AllArgsConstructor
public class TemplateDocumensoService {
  private static final int PAGE_SIZE = 100;
  private static final int MAX_PAGES = 50;

  private final DocumensoClient documensoClient;
  private final TemplateDocumensoRepository templateDocumensoRepository;
  private final DocumensoDocumentRepository documensoDocumentRepository;
  private final UserRepository userRepository;

  private record RemoteTemplates(
      List<TemplateFindTemplates200ResponseDataInner> templates, boolean listedInFull) {}

  @SneakyThrows
  public List<TemplateDocumenso> syncTemplates() {
    var remote = fetchAllRemoteTemplates();
    var synced = remote.templates().stream().map(this::upsert).toList();
    if (remote.listedInFull()) {
      prune(synced.stream().map(TemplateDocumenso::getDocumensoTemplateId).toList());
    }
    return synced;
  }

  public List<TemplateDocumenso> getAll(PageFromOne page, BoundedPageSize pageSize) {
    var pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return templateDocumensoRepository.findAll(pageable).getContent();
  }

  private RemoteTemplates fetchAllRemoteTemplates() {
    var remoteTemplates = new ArrayList<TemplateFindTemplates200ResponseDataInner>();
    for (var page = 1; page <= MAX_PAGES; page++) {
      var data = documensoClient.findTemplates(null, page, PAGE_SIZE).getData();
      if (data == null || data.isEmpty()) {
        return new RemoteTemplates(remoteTemplates, true);
      }
      remoteTemplates.addAll(data);
      if (data.size() < PAGE_SIZE) {
        return new RemoteTemplates(remoteTemplates, true);
      }
    }
    log.warn(
        "Stopped listing Documenso templates after {} pages: pruning is skipped to avoid deleting"
            + " templates that were never listed",
        MAX_PAGES);
    return new RemoteTemplates(remoteTemplates, false);
  }

  private void prune(List<Long> remoteIds) {
    var stale =
        remoteIds.isEmpty()
            ? templateDocumensoRepository.findAll()
            : templateDocumensoRepository.findAllByDocumensoTemplateIdNotIn(remoteIds);
    for (var template : stale) {
      if (documensoDocumentRepository.existsByTemplate_Id(template.getId())) {
        log.warn(
            "Documenso template {} is gone from Documenso but still bears documents: keeping it",
            template.getDocumensoTemplateId());
        continue;
      }
      log.info(
          "Dropping Documenso template {}, gone from Documenso", template.getDocumensoTemplateId());
      templateDocumensoRepository.delete(template);
    }
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
