package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.StudentTranscriptVersion;
import school.hei.haapi.model.Transcript;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.TranscriptService;
import school.hei.haapi.service.UserService;

import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class StudentTranscriptVersionMapper {

  private final UserService userService;
  private final TranscriptService transcriptService;
  public school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion toRest(StudentTranscriptVersion studentTranscriptVersion) {
    final Transcript transcript = studentTranscriptVersion.getTranscript();
    final User responsible = studentTranscriptVersion.getResponsible();
    if (Objects.isNull(transcript) || Objects.isNull(responsible)){
      throw new NotFoundException("Transcript or User not found");
    } else {
    var restStudentTranscriptVersion = new school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion();
    restStudentTranscriptVersion.setId(studentTranscriptVersion.getId());
    restStudentTranscriptVersion.setTranscriptId(transcript.getId());
    restStudentTranscriptVersion.setRef(studentTranscriptVersion.getRef());
    restStudentTranscriptVersion.setCreatedByUserId(responsible.getId());
    restStudentTranscriptVersion.setCreatedByUserRole(studentTranscriptVersion.getResponsible().getRole().name());
    restStudentTranscriptVersion.setCreationDatetime(studentTranscriptVersion.getCreationDatetime());
      return restStudentTranscriptVersion;
    }
  }

  public StudentTranscriptVersion toDomain(school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion restStudentTranscriptVersion, String studentId) {
    final Transcript transcript = transcriptService.getByIdAndStudentId(restStudentTranscriptVersion.getTranscriptId(), studentId);
    final User responsible = userService.getById(restStudentTranscriptVersion.getCreatedByUserId());
    if (Objects.isNull(transcript) || Objects.isNull(responsible)){
      throw new NotFoundException("Transcript or User not found");
    } else {
    return StudentTranscriptVersion.builder()
        .id(restStudentTranscriptVersion.getId())
            .transcript(transcript)
            .ref(restStudentTranscriptVersion.getRef())
            .responsible(responsible)
            .creationDatetime(restStudentTranscriptVersion.getCreationDatetime())
            .build();
    }
  }
}
