package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.TranscriptVersion;

import java.util.List;

@Repository
public interface TranscriptVersionRepository extends JpaRepository<TranscriptVersion,String> {
    List<TranscriptVersion> findAllByTranscriptId(String transcriptId,Pageable pageable);
    List<TranscriptVersion> findAllByTranscriptIdOrderByCreationDatetimeDesc(String transcriptId);
}
