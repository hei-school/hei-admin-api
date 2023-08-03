package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.TranscriptClaim;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.TranscriptClaimValidator;
import school.hei.haapi.repository.TranscriptClaimRepository;

import java.util.List;
import java.util.Objects;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class TranscriptClaimService {
    private final TranscriptClaimRepository transcriptClaimRepository;
    private final TranscriptClaimValidator transcriptClaimValidator;
    public TranscriptClaim getById(String transcriptId) {
        return transcriptClaimRepository.getById(transcriptId);
    }
    public TranscriptClaim  findByVersionIdAndClaimId(String studentId, String transcriptId, String versionId,String claimId){

        return transcriptClaimRepository.findByTranscriptVersionTranscriptStudentIdAndTranscriptVersionTranscriptIdAndTranscriptVersionIdAndId(studentId,transcriptId,versionId,claimId)
                .orElseThrow(() -> new NotFoundException("Transcript claim id" + claimId + "not found"));
    }
    public List<TranscriptClaim> getAllByVersionId(String studentId, String transcriptId, String versionId, PageFromOne page, BoundedPageSize pageSize){
        Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
        return transcriptClaimRepository.findAllByTranscriptVersionTranscriptStudentIdAndTranscriptVersionTranscriptIdAndTranscriptVersionId(studentId,transcriptId,versionId, pageable);
    }


    public TranscriptClaim save(TranscriptClaim domain) {
        transcriptClaimValidator.accept(domain);
        return transcriptClaimRepository.save(domain);
    }
}
