package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.TranscriptClaim;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface TranscriptClaimRepository extends JpaRepository< TranscriptClaim,String> {
    List<TranscriptClaim> findAllByVersionId(String versionId, Pageable pageable);
    TranscriptClaim findFirstByVersionIdOrderByCreationDatetimeDesc(String versionId);
}
