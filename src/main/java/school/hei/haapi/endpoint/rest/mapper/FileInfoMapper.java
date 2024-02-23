package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.mapper.constant.PresignedUrlDurationConstant;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.service.aws.FileService;

@Component
@AllArgsConstructor
public class FileInfoMapper {
  private final FileService fileService;
  private final PresignedUrlDurationConstant constant;

  public school.hei.haapi.endpoint.rest.model.FileInfo toRest(FileInfo fileInfo) {
    String presignedUrl = fileInfo.getFilePath();
    String accessibleFileUrl =
        presignedUrl != null
            ? fileService.getPresignedUrl(fileInfo.getFilePath(), constant.presignedUrlDuration)
            : null;
    return new school.hei.haapi.endpoint.rest.model.FileInfo()
        .id(fileInfo.getId())
        .fileType(fileInfo.getFileType())
        .name(fileInfo.getName())
        .fileUrl(accessibleFileUrl)
        .creationDatetime(fileInfo.getCreationDatetime());
  }
}
