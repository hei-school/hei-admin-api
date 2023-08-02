package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.StudentTranscriptClaim;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.StudentTranscriptVersionService;
import school.hei.haapi.service.TranscriptService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
@Slf4j
public class StudentTranscriptClaimMapper {

  private final UserService userService;
  private final TranscriptService transcriptService;
  private final StudentTranscriptVersionService studentTranscriptVersionService;
  public school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim toRest(StudentTranscriptClaim studentTranscriptClaim) {
  if (studentTranscriptClaim.getTranscript() == null || studentTranscriptClaim.getTranscriptVersion() == null){
    throw new NotFoundException("Claim not found");
  } else {
    var restStudentTranscriptClaim = new school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim();
    restStudentTranscriptClaim.setId(studentTranscriptClaim.getId());
    restStudentTranscriptClaim.setTranscriptId(studentTranscriptClaim.getTranscript().getId());
    restStudentTranscriptClaim.setTranscriptVersionId(studentTranscriptClaim.getTranscriptVersion().getId());
    restStudentTranscriptClaim.setStatus(studentTranscriptClaim.getStatus());
    restStudentTranscriptClaim.setCreationDatetime(studentTranscriptClaim.getCreationDatetime());
    restStudentTranscriptClaim.setClosedDatetime(studentTranscriptClaim.getClosedDatetime());
    restStudentTranscriptClaim.setReason(studentTranscriptClaim.getReason());
    return restStudentTranscriptClaim;
    }
  }

  public StudentTranscriptClaim toDomain(school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim restStudentTranscriptClaim, String studentId) {
    return StudentTranscriptClaim.builder()
        .id(restStudentTranscriptClaim.getId())
            .transcript(transcriptService.getByIdAndStudentId(restStudentTranscriptClaim.getTranscriptId(), studentId))
            .transcriptVersion(studentTranscriptVersionService.getByIdAndStudentIdAndTranscriptId(
                    restStudentTranscriptClaim.getTranscriptVersionId(),
                    studentId,
                    restStudentTranscriptClaim.getTranscriptId()
            ))
            .status(restStudentTranscriptClaim.getStatus())
            .creationDatetime(restStudentTranscriptClaim.getCreationDatetime())
            .closedDatetime(restStudentTranscriptClaim.getClosedDatetime())
            .reason(restStudentTranscriptClaim.getReason())
            .build();
  }
}
