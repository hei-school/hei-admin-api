package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.StudentTranscriptVersion;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.TranscriptService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
@Slf4j
public class StudentTranscriptVersionMapper {

  private final UserService userService;
  private final TranscriptService transcriptService;
  public school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion toRest(StudentTranscriptVersion studentTranscriptVersion) {
  if (studentTranscriptVersion.getTranscript() == null || studentTranscriptVersion.getResponsible() == null){
    throw new NotFoundException("Transcript not found");
  } else {
    var restStudentTranscriptVersion = new school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion();
    restStudentTranscriptVersion.setId(studentTranscriptVersion.getId());
    restStudentTranscriptVersion.setTranscriptId(studentTranscriptVersion.getTranscript().getId());
    restStudentTranscriptVersion.setRef(studentTranscriptVersion.getRef());
    restStudentTranscriptVersion.setCreatedByUserId(studentTranscriptVersion.getResponsible().getId());
    restStudentTranscriptVersion.setCreatedByUserRole(studentTranscriptVersion.getResponsible().getRole().name());
    restStudentTranscriptVersion.setCreationDatetime(studentTranscriptVersion.getCreationDatetime());
    return restStudentTranscriptVersion;
    }
  }

  public StudentTranscriptVersion toDomain(school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion restStudentTranscriptVersion, String studentId) {
    return StudentTranscriptVersion.builder()
        .id(restStudentTranscriptVersion.getId())
            .transcript(transcriptService.getByIdAndStudentId(restStudentTranscriptVersion.getTranscriptId(), studentId))
            .ref(restStudentTranscriptVersion.getRef())
            .responsible(userService.getById(restStudentTranscriptVersion.getCreatedByUserId()))
            .creationDatetime(restStudentTranscriptVersion.getCreationDatetime())
            .build();
  }
}
