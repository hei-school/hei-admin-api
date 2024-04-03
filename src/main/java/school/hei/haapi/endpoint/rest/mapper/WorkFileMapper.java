package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.endpoint.rest.mapper.FileInfoMapper.ONE_DAY_DURATION_AS_LONG;
import static school.hei.haapi.endpoint.rest.model.FileType.WORK_DOCUMENT;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.FileInfo;
import school.hei.haapi.model.WorkFile;
import school.hei.haapi.service.aws.FileService;

@Component
@AllArgsConstructor
public class WorkFileMapper {
  private final FileService fileService;

  public FileInfo toRest(WorkFile domain) {
    String filePath = domain.getFilePath();
    String presignedUrl =
        filePath != null
            ? fileService.getPresignedUrl(domain.getFilePath(), ONE_DAY_DURATION_AS_LONG)
            : null;
    return new school.hei.haapi.endpoint.rest.model.FileInfo()
        .id(domain.getId())
        .fileType(WORK_DOCUMENT)
        .name(domain.getFilename())
        .fileUrl(presignedUrl)
        .creationDatetime(domain.getCreationDatetime());
  }
}
