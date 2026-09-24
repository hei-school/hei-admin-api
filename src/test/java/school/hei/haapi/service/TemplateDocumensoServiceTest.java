package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.TemplateDocumenso;
import school.hei.haapi.repository.DocumensoDocumentRepository;
import school.hei.haapi.repository.TemplateDocumensoRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.RemoteFolder;
import school.hei.haapi.service.documenso.gen.model.TemplateFindTemplates200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateFindTemplates200ResponseDataInner;

class TemplateDocumensoServiceTest {
  private final DocumensoClient documensoClient = mock(DocumensoClient.class);
  private final TemplateDocumensoRepository templateRepository =
      mock(TemplateDocumensoRepository.class);
  private final DocumensoDocumentRepository documentRepository =
      mock(DocumensoDocumentRepository.class);
  private final UserRepository userRepository = mock(UserRepository.class);

  private final TemplateDocumensoService subject =
      new TemplateDocumensoService(
          documensoClient, templateRepository, documentRepository, userRepository);

  private static TemplateFindTemplates200ResponseDataInner aRemoteTemplate(long id, String title) {
    var remote = new TemplateFindTemplates200ResponseDataInner();
    remote.setId(BigDecimal.valueOf(id));
    remote.setTitle(title);
    return remote;
  }

  private static TemplateFindTemplates200Response aPageOf(
      TemplateFindTemplates200ResponseDataInner... templates) {
    var page = new TemplateFindTemplates200Response();
    page.setData(List.of(templates));
    return page;
  }

  @BeforeEach
  void noTemplateAnywhereByDefault() {
    when(templateRepository.findByDocumensoTemplateId(any())).thenReturn(Optional.empty());
    when(templateRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    when(documensoClient.findTemplates(any(), anyInt(), anyInt())).thenReturn(aPageOf());
    when(documensoClient.findFolders(any(), any())).thenReturn(List.of());
    when(templateRepository.findAll()).thenReturn(List.of());
  }

  @Test
  void a_template_filed_in_a_folder_is_synced_too() {
    when(documensoClient.findFolders(null, "TEMPLATE"))
        .thenReturn(List.of(new RemoteFolder("f1", "Modèles de fiches", null, "TEMPLATE")));
    when(documensoClient.findTemplatesOfFolder(eq("f1"), anyInt(), anyInt()))
        .thenReturn(aPageOf(aRemoteTemplate(7L, "Fiche 2027 L3.pdf")));

    var synced = subject.syncTemplates();

    assertEquals(1, synced.size());
    assertEquals("Fiche 2027 L3.pdf", synced.getFirst().getTitle());
  }

  @Test
  void a_template_of_a_nested_folder_is_synced_too() {
    when(documensoClient.findFolders(null, "TEMPLATE"))
        .thenReturn(List.of(new RemoteFolder("f1", "Modèles", null, "TEMPLATE")));
    when(documensoClient.findFolders("f1", "TEMPLATE"))
        .thenReturn(List.of(new RemoteFolder("f2", "2027", "f1", "TEMPLATE")));
    when(documensoClient.findTemplatesOfFolder(eq("f1"), anyInt(), anyInt())).thenReturn(aPageOf());
    when(documensoClient.findTemplatesOfFolder(eq("f2"), anyInt(), anyInt()))
        .thenReturn(aPageOf(aRemoteTemplate(9L, "Fiche 2027 L3.pdf")));

    assertEquals(1, subject.syncTemplates().size());
  }

  @Test
  void an_empty_listing_never_empties_the_local_catalogue() {
    when(templateRepository.findAll())
        .thenReturn(List.of(TemplateDocumenso.builder().documensoTemplateId(7L).build()));

    subject.syncTemplates();

    verify(templateRepository, never()).delete(any());
  }

  @Test
  void a_listing_that_never_ends_stops_the_pruning_rather_than_the_sync() {
    /* every page comes back full, so the listing is never known to be complete: deleting on that
     * basis would drop templates we simply never saw */
    var aFullPage =
        new TemplateFindTemplates200Response()
            .data(
                java.util.stream.IntStream.range(0, 100)
                    .mapToObj(i -> aRemoteTemplate(i, "Fiche " + i))
                    .toList());
    when(documensoClient.findTemplates(any(), anyInt(), anyInt())).thenReturn(aFullPage);
    when(templateRepository.findAll())
        .thenReturn(List.of(TemplateDocumenso.builder().documensoTemplateId(999L).build()));

    subject.syncTemplates();

    verify(templateRepository, never()).delete(any());
  }
}
