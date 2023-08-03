package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.TranscriptClaim;

import java.util.List;

@Repository
public interface TranscriptClaimRepository extends JpaRepository< TranscriptClaim,String> {
    List<TranscriptClaim> findAllByTranscriptVersionId(String versionId, Pageable pageable);
}
