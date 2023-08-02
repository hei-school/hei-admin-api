package school.hei.haapi.endpoint.rest.controller;

import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.StudentTranscriptClaimMapper;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim;
import school.hei.haapi.service.StudentTranscriptClaimService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class StudentTranscriptClaimController {

  private final StudentTranscriptClaimService studentTranscriptClaimService;
  private final StudentTranscriptClaimMapper studentTranscriptClaimMapper;

  @GetMapping(value = "/students/{studentId}/transcripts/{transcriptId}/versions/{versionId}/claims")
  public List<StudentTranscriptClaim> getStudentTranscriptClaims(@PathVariable String studentId,
                                                                 @PathVariable String transcriptId,
                                                                 @PathVariable String versionId   ) {
    return studentTranscriptClaimService.getAllByStudentIdAndTranscriptIdAndVersionId(
                    versionId, transcriptId, studentId).stream()
            .map(studentTranscriptClaimMapper::toRest)
            .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping(value = "/students/{studentId}/transcripts/{transcriptId}/versions/{versionId}/claims/{claimId}")
  public StudentTranscriptClaim getStudentClaimOfTranscriptVersion(@PathVariable String studentId,
                                                              @PathVariable String transcriptId,
                                                              @PathVariable String versionId,
                                                              @PathVariable String claimId) {
    return studentTranscriptClaimMapper.toRest(studentTranscriptClaimService.
            getByIdAndStudentIdAndTranscriptIdAndVersionId(
                    claimId, versionId, transcriptId, studentId));
  }

  @PutMapping(value = "/students/{studentId}/transcripts/{transcriptId}/versions/{versionId}/claims/{claimId}")
  public StudentTranscriptClaim putStudentClaimsOfTranscriptVersion(
          @RequestBody StudentTranscriptClaim studentTranscriptClaim,
          @PathVariable String studentId,
          @PathVariable String transcriptId,
          @PathVariable String versionId,
          @PathVariable String claimId) throws NotFoundException {
    school.hei.haapi.model.StudentTranscriptClaim toSave = studentTranscriptClaimMapper.toDomain(studentTranscriptClaim, studentId);
    return studentTranscriptClaimMapper.toRest(studentTranscriptClaimService.save(
                    toSave, claimId, versionId, transcriptId, studentId));
  }


}
