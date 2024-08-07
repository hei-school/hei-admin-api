package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.endpoint.rest.mapper.FileInfoMapper.ONE_DAY_DURATION_AS_LONG;
import static school.hei.haapi.endpoint.rest.model.FileType.WORK_DOCUMENT;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.WorkDocumentInfo;
import school.hei.haapi.model.WorkDocument;
import school.hei.haapi.service.aws.FileService;

@Component
@AllArgsConstructor
public class WorkDocumentMapper {
  private final FileService fileService;

  public WorkDocumentInfo toRest(WorkDocument domain) {
    String filePath = domain.getFilePath();
    String presignedUrl =
        filePath != null
            ? fileService.getPresignedUrl(domain.getFilePath(), ONE_DAY_DURATION_AS_LONG)
            : null;
    return new school.hei.haapi.endpoint.rest.model.WorkDocumentInfo()
        .id(domain.getId())
        .fileType(WORK_DOCUMENT)
        .name(domain.getFilename())
        .fileUrl(presignedUrl)
        .professionalExperience(domain.getProfessionalExperienceType())
        .creationDatetime(domain.getCreationDatetime());
  }
}
