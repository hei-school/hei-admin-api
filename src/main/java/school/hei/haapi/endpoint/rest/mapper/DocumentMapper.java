package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Document;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.service.aws.FileService;

@Component
@AllArgsConstructor
public class DocumentMapper {
  private final FileService fileService;

  public Document toRest(FileInfo fileInfo) {
    String fileKeyUrl = fileInfo.getFileKeyUrl();
    String accessibleFileUrl =
        fileKeyUrl != null ? fileService.getPresignedUrl(fileInfo.getFileKeyUrl(), 86400L) : null;
    return new Document()
        .id(fileInfo.getId())
        .fileType(fileInfo.getFileType())
        .name(fileInfo.getName())
        .fileUrl(accessibleFileUrl)
        .creationDatetime(fileInfo.getCreationDatetime());
  }
}
