package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.OwnCloudPermission;
import school.hei.haapi.endpoint.rest.model.ShareInfo;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.notEntity.OcsData;
import school.hei.haapi.service.aws.FileService;

import java.util.EnumSet;

import static school.hei.haapi.endpoint.rest.model.OwnCloudPermission.READ;

@Component
@AllArgsConstructor
public class FileInfoMapper {
  private final FileService fileService;
  public static final long ONE_DAY_DURATION_AS_LONG = 84600L;

  public school.hei.haapi.endpoint.rest.model.FileInfo toRest(FileInfo fileInfo) {
    String filePath = fileInfo.getFilePath();
    String presignedUrl =
        filePath != null
            ? fileService.getPresignedUrl(fileInfo.getFilePath(), ONE_DAY_DURATION_AS_LONG)
            : null;
    return new school.hei.haapi.endpoint.rest.model.FileInfo()
        .id(fileInfo.getId())
        .fileType(fileInfo.getFileType())
        .name(fileInfo.getName())
        .fileUrl(presignedUrl)
        .creationDatetime(fileInfo.getCreationDatetime());
  }

  public ShareInfo toShareInfo(OcsData ocsData) {
      EnumSet<OwnCloudPermission> permissions = EnumSet.allOf(OwnCloudPermission.class);
    return new ShareInfo()
        .path(ocsData.getPath())
        .url(ocsData.getUrl())
        .permission(permissions.stream().toList())
        .expiration(ocsData.getExpiration());
  }
}
