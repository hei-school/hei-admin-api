package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.model.DocumensoFolder;
import school.hei.haapi.repository.DocumensoFolderRepository;
import school.hei.haapi.service.documenso.CreateRemoteFolder;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.RemoteFolder;

class DocumensoFolderServiceTest {
  private static final List<String> A_PATH =
      List.of(DocumensoFolderService.ROOT_FOLDER_NAME, "2026 - 2027", "L3");

  private final DocumensoClient documensoClient = mock(DocumensoClient.class);
  private final DocumensoFolderRepository folderRepository = mock(DocumensoFolderRepository.class);

  private final DocumensoFolderService subject =
      new DocumensoFolderService(documensoClient, folderRepository);

  private static RemoteFolder aRemoteFolder(String id, String name, String parentId) {
    return new RemoteFolder(id, name, parentId, "DOCUMENT");
  }

  private void givenNothingKnownLocally() {
    when(folderRepository.findByPath(any())).thenReturn(Optional.empty());
  }

  @Test
  void a_known_path_is_not_asked_to_documenso_again() {
    when(folderRepository.findByPath("Fiches d'engagement")).thenReturn(Optional.empty());
    when(folderRepository.findByPath(any()))
        .thenReturn(
            Optional.of(
                DocumensoFolder.builder().path("whatever").documensoFolderId("f3").build()));

    assertEquals("f3", subject.resolveFolderId(A_PATH));
    verifyNoInteractions(documensoClient);
  }

  @Test
  void a_folder_made_by_hand_is_reused_rather_than_duplicated() {
    givenNothingKnownLocally();
    when(documensoClient.findFolders(null, "DOCUMENT"))
        .thenReturn(List.of(aRemoteFolder("root", DocumensoFolderService.ROOT_FOLDER_NAME, null)));
    when(documensoClient.findFolders("root", "DOCUMENT"))
        .thenReturn(List.of(aRemoteFolder("year", "2026 - 2027", "root")));
    when(documensoClient.findFolders("year", "DOCUMENT"))
        .thenReturn(List.of(aRemoteFolder("level", "L3", "year")));

    assertEquals("level", subject.resolveFolderId(A_PATH));
    verify(documensoClient, never()).createFolder(any());
  }

  @Test
  void a_missing_chain_is_created_from_the_root_down() {
    givenNothingKnownLocally();
    when(documensoClient.findFolders(any(), eq("DOCUMENT"))).thenReturn(List.of());
    when(documensoClient.createFolder(any()))
        .thenAnswer(
            call -> {
              var asked = call.getArgument(0, CreateRemoteFolder.class);
              return aRemoteFolder(asked.name(), asked.name(), asked.parentId());
            });

    assertEquals("L3", subject.resolveFolderId(A_PATH));

    var created = ArgumentCaptor.forClass(CreateRemoteFolder.class);
    verify(documensoClient, org.mockito.Mockito.times(3)).createFolder(created.capture());
    var asked = created.getAllValues();
    assertEquals(DocumensoFolderService.ROOT_FOLDER_NAME, asked.get(0).name());
    assertEquals(null, asked.get(0).parentId(), "the root folder hangs off nothing");
    assertEquals("2026 - 2027", asked.get(1).name());
    assertEquals(DocumensoFolderService.ROOT_FOLDER_NAME, asked.get(1).parentId());
    assertEquals("L3", asked.get(2).name());
    assertEquals("2026 - 2027", asked.get(2).parentId());
  }

  @Test
  void every_resolved_folder_is_remembered_by_its_full_path() {
    givenNothingKnownLocally();
    when(documensoClient.findFolders(any(), eq("DOCUMENT"))).thenReturn(List.of());
    when(documensoClient.createFolder(any()))
        .thenAnswer(
            call -> {
              var asked = call.getArgument(0, CreateRemoteFolder.class);
              return aRemoteFolder(asked.name(), asked.name(), asked.parentId());
            });

    subject.resolveFolderId(A_PATH);

    var saved = ArgumentCaptor.forClass(DocumensoFolder.class);
    verify(folderRepository, org.mockito.Mockito.times(3)).save(saved.capture());
    assertEquals(
        List.of(
            "Fiches d'engagement",
            "Fiches d'engagement/2026 - 2027",
            "Fiches d'engagement/2026 - 2027/L3"),
        saved.getAllValues().stream().map(DocumensoFolder::getPath).toList());
  }
}
