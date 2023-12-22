package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.TranscriptClaim;

@Repository
public interface TranscriptClaimRepository extends JpaRepository<TranscriptClaim, String> {
  List<TranscriptClaim> findAllByTranscriptVersionId(String versionId, Pageable pageable);

  @Query(
      "select t from TranscriptClaim t where "
          + "t.transcriptVersion.transcript.student.id = :studentId "
          + "and t.transcriptVersion.transcript.id = :transcriptId "
          + "and t.transcriptVersion.id = :versionId")
  List<TranscriptClaim> findTranscriptClaimsByCriteria(
      String studentId, String transcriptId, String versionId, Pageable pageable);

  @Query(
      "select t from TranscriptClaim t where "
          + "t.transcriptVersion.transcript.student.id = :studentId "
          + "and t.transcriptVersion.transcript.id = :transcriptId "
          + "and t.transcriptVersion.id = :versionId and t.id = :claimId")
  Optional<TranscriptClaim> findTranscriptClaimWithCriteria(
      String studentId, String transcriptId, String versionId, String claimId);
}
