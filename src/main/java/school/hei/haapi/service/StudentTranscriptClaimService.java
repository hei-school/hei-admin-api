package school.hei.haapi.service;

import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.StudentTranscriptClaim;
import school.hei.haapi.repository.StudentTranscriptClaimRepository;
import school.hei.haapi.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class StudentTranscriptClaimService {

  private final StudentTranscriptClaimRepository studentTranscriptClaimRepository;

  public StudentTranscriptClaim getByIdAndStudentIdAndTranscriptIdAndVersionId(
          String claimId, String versionId, String studentId, String transcriptId) {
    return studentTranscriptClaimRepository.getByIdAndStudentIdAndTranscriptIdAndVersionId(claimId, versionId, transcriptId, studentId);
  }

  public List<StudentTranscriptClaim> getAllByStudentIdAndTranscriptIdAndVersionId(
          String versionId, String studentId, String transcriptId) {
    return studentTranscriptClaimRepository.getAllByStudentIdAndTranscriptIdAndVersionId(versionId, transcriptId, studentId);
  }

  public StudentTranscriptClaim save(StudentTranscriptClaim toSave, String claimId,
                                     String versionId,
                                     String transcriptId,
                                     String studentId) throws NotFoundException {
    StudentTranscriptClaim studentTranscriptClaim = studentTranscriptClaimRepository.getByIdAndStudentIdAndTranscriptIdAndVersionId(
            claimId, versionId, transcriptId, studentId);
    if(Objects.equals(studentTranscriptClaim, null)){
      throw new NotFoundException("Resource does not exists");
    } else {
      return studentTranscriptClaimRepository.save(toSave);
    }
  }
}
