package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.TranscriptClaim;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranscriptClaimRepository extends JpaRepository< TranscriptClaim,String> {
    List<TranscriptClaim> findAllByTranscriptVersionId(String versionId, Pageable pageable);

    List<TranscriptClaim> findAllByTranscriptVersionTranscriptStudentIdAndTranscriptVersionTranscriptIdAndTranscriptVersionId(String studentId, String transcriptId, String versionId, Pageable pageable);

    Optional<TranscriptClaim> findByTranscriptVersionTranscriptStudentIdAndTranscriptVersionTranscriptIdAndTranscriptVersionIdAndId(String studentId, String transcriptId, String versionId, String claimId);
}
