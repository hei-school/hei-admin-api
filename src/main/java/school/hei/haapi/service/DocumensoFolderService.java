package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.DocumensoFolder;
import school.hei.haapi.repository.DocumensoFolderRepository;
import school.hei.haapi.service.documenso.CreateRemoteFolder;
import school.hei.haapi.service.documenso.DocumensoClient;
import school.hei.haapi.service.documenso.RemoteFolder;

@Service
@AllArgsConstructor
@Slf4j
public class DocumensoFolderService {
  public static final String ROOT_FOLDER_NAME = "Fiches d'engagement";
  private static final String DOCUMENT_FOLDER_TYPE = "DOCUMENT";
  private static final String PATH_SEPARATOR = "/";

  private final DocumensoClient documensoClient;
  private final DocumensoFolderRepository documensoFolderRepository;

  public String resolveFolderId(List<String> segments) {
    String parentId = null;
    var walked = new StringBuilder();
    for (var segment : segments) {
      if (!walked.isEmpty()) {
        walked.append(PATH_SEPARATOR);
      }
      walked.append(segment);
      parentId = resolveSegment(walked.toString(), segment, parentId);
    }
    return parentId;
  }

  private String resolveSegment(String path, String name, String parentId) {
    var known = documensoFolderRepository.findByPath(path);
    if (known.isPresent()) {
      return known.get().getDocumensoFolderId();
    }

    var remoteId =
        documensoClient.findFolders(parentId, DOCUMENT_FOLDER_TYPE).stream()
            .filter(folder -> name.equals(folder.getName()))
            .findFirst()
            .map(RemoteFolder::getId)
            .orElseGet(() -> createRemote(path, name, parentId));

    documensoFolderRepository.save(
        DocumensoFolder.builder().path(path).documensoFolderId(remoteId).build());
    return remoteId;
  }

  private String createRemote(String path, String name, String parentId) {
    log.info("Creating Documenso folder {}", path);
    var created =
        documensoClient.createFolder(new CreateRemoteFolder(name, parentId, DOCUMENT_FOLDER_TYPE));
    if (created == null || created.getId() == null || created.getId().isBlank()) {
      throw new IllegalStateException("Documenso created folder " + path + " without an id");
    }
    return created.getId();
  }
}
