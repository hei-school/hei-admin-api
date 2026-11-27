package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.service.aws.FileService;

@Component
@AllArgsConstructor
public class ComplaintAttachmentMapper {
  private final FileService fileService;
  private final UserMapper userMapper;

  public school.hei.haapi.endpoint.rest.model.ComplaintAttachment toRest(
      school.hei.haapi.model.ComplaintAttachment attachment) {
    return new school.hei.haapi.endpoint.rest.model.ComplaintAttachment()
        .id(attachment.getId())
        .fileName(attachment.getFileName())
        .contentType(attachment.getContentType())
        .fileUrl(
            fileService.getPresignedUrl(
                attachment.getFilePath(), FileInfoMapper.ONE_DAY_DURATION_AS_LONG))
        .uploadedBy(userMapper.toIdentifier(attachment.getUploadedBy()))
        .createdAt(attachment.getCreatedAt());
  }

  public List<school.hei.haapi.endpoint.rest.model.ComplaintAttachment> toRest(
      List<school.hei.haapi.model.ComplaintAttachment> attachments) {
    return attachments.stream().map(this::toRest).toList();
  }
}
