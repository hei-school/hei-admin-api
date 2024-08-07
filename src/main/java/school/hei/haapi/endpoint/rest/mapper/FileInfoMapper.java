package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.service.utils.OwnCloudUtils.findOwnCloudPermissionsFromValue;

import java.util.EnumSet;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.OwnCloudPermission;
import school.hei.haapi.endpoint.rest.model.ShareInfo;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.notEntity.OcsData;
import school.hei.haapi.service.aws.FileService;

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
    EnumSet<OwnCloudPermission> permissions =
        findOwnCloudPermissionsFromValue(ocsData.getOcs().getData().getPermissions());
    return new ShareInfo()
        .path(ocsData.getOcs().getData().getPath())
        .password(ocsData.getOcs().getData().getPassword())
        .url(ocsData.getOcs().getData().getUrl())
        .permission(permissions.stream().toList())
        .expiration(ocsData.getOcs().getData().getExpiration());
  }
}
