package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Document;
import school.hei.haapi.model.File;
import school.hei.haapi.service.aws.FileService;

@Component
@AllArgsConstructor
public class StudentFileMapper {
  private final FileService fileService;

  public Document toRest(File file) {
    return new Document()
        .id(file.getId())
        .fileType(file.getFileType())
        .name(file.getName())
        .fileUrl(fileService.getPresignedUrl(file.getFileKeyUrl(), 86400L))
        .creationDatetime(file.getCreationDatetime());
  }
}
