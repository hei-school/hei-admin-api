package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.StudentTranscriptClaim;
import school.hei.haapi.model.StudentTranscriptVersion;
import school.hei.haapi.model.Transcript;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.StudentTranscriptVersionService;
import school.hei.haapi.service.TranscriptService;
import school.hei.haapi.service.UserService;

import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class StudentTranscriptClaimMapper {

  private final TranscriptService transcriptService;
  private final StudentTranscriptVersionService studentTranscriptVersionService;
  public school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim toRest(StudentTranscriptClaim studentTranscriptClaim) {
    Transcript transcript = studentTranscriptClaim.getTranscript();
    StudentTranscriptVersion version = studentTranscriptClaim.getTranscriptVersion();

    if (Objects.isNull(transcript)) {
      throw new NotFoundException("Transcript not found");
    } else if (Objects.isNull(version)) {
      throw new NotFoundException("Version not found");
    } else {
      var restStudentTranscriptClaim = new school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim();
      restStudentTranscriptClaim.setId(studentTranscriptClaim.getId());
      restStudentTranscriptClaim.setTranscriptId(transcript.getId());
      restStudentTranscriptClaim.setTranscriptVersionId(version.getId());
      restStudentTranscriptClaim.setStatus(studentTranscriptClaim.getStatus());
      restStudentTranscriptClaim.setCreationDatetime(studentTranscriptClaim.getCreationDatetime());
      restStudentTranscriptClaim.setClosedDatetime(studentTranscriptClaim.getClosedDatetime());
      restStudentTranscriptClaim.setReason(studentTranscriptClaim.getReason());
      return restStudentTranscriptClaim;
    }
  }

  public StudentTranscriptClaim toDomain(school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim restStudentTranscriptClaim, String studentId) {
    Transcript transcript = transcriptService.getByIdAndStudentId(restStudentTranscriptClaim.getTranscriptId(), studentId);
    StudentTranscriptVersion version = studentTranscriptVersionService.getByIdAndStudentIdAndTranscriptId(
            restStudentTranscriptClaim.getTranscriptVersionId(),
            restStudentTranscriptClaim.getTranscriptId(),
            studentId
    );
    if (Objects.isNull(transcript)) {
      throw new NotFoundException("Transcript not found");
    } else if (Objects.isNull(version)) {
      throw new NotFoundException("Version not found");
    } else {
      return StudentTranscriptClaim.builder()
              .id(restStudentTranscriptClaim.getId())
              .transcript(transcript)
              .transcriptVersion(version)
              .status(restStudentTranscriptClaim.getStatus())
              .creationDatetime(restStudentTranscriptClaim.getCreationDatetime())
              .closedDatetime(restStudentTranscriptClaim.getClosedDatetime())
              .reason(restStudentTranscriptClaim.getReason())
              .build();
    }
  }
}
