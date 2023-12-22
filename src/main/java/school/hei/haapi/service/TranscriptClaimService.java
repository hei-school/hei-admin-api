package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.TranscriptClaim;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.TranscriptClaimValidator;
import school.hei.haapi.repository.TranscriptClaimRepository;
import school.hei.haapi.repository.TranscriptVersionRepository;

@Service
@AllArgsConstructor
public class TranscriptClaimService {
  private final TranscriptVersionRepository transcriptVersionRepository;
  private final TranscriptClaimRepository transcriptClaimRepository;
  private final TranscriptClaimValidator transcriptClaimValidator;

  public TranscriptClaim getById(String transcriptId) {
    return transcriptClaimRepository.getById(transcriptId);
  }

  public TranscriptClaim findByVersionIdAndClaimId(
      String studentId, String transcriptId, String versionId, String claimId) {
    return transcriptClaimRepository
        .findTranscriptClaimWithCriteria(studentId, transcriptId, versionId, claimId)
        .orElseThrow(() -> new NotFoundException("Transcript claim id " + claimId + " not found"));
  }

  public List<TranscriptClaim> getAllByVersionId(
      String studentId,
      String transcriptId,
      String versionId,
      PageFromOne page,
      BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return transcriptClaimRepository.findTranscriptClaimsByCriteria(
        studentId, transcriptId, versionId, pageable);
  }

  public TranscriptClaim save(
      TranscriptClaim domain,
      String studentId,
      String transcriptId,
      String versionId,
      String claimId) {
    // validate each attribute before saving it in database
    transcriptClaimValidator.accept(domain);
    // check if the claim already exist and all information given are corrects - to modify
    if (transcriptClaimRepository
        .findTranscriptClaimWithCriteria(studentId, transcriptId, versionId, claimId)
        .isPresent()) {
      return transcriptClaimRepository.save(domain);
      // check if this claim doesn't exist and all information given are corrects - to modify
    } else if (transcriptClaimRepository.findById(claimId).isEmpty()
        && transcriptVersionRepository
            .findByTranscriptStudentIdAndTranscriptIdAndId(studentId, transcriptId, versionId)
            .isPresent()) {
      return transcriptClaimRepository.save(domain);
    } else {
      throw new NotFoundException(
          "Some of the information about the student is incorrect: student_id or transcript_id or"
              + " version_id");
    }
  }
}
