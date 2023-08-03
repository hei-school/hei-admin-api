package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.TranscriptVersion;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranscriptVersionRepository extends JpaRepository<TranscriptVersion,String> {
    List<TranscriptVersion> findAllByTranscriptStudentIdAndTranscriptId(String sId, String tId, Pageable pageable);

    Optional<TranscriptVersion> findFirstByTranscriptStudentIdAndTranscriptIdOrderByRefDesc(String studentId,String transcriptId);
    Optional<TranscriptVersion> findByTranscriptStudentIdAndTranscriptIdAndId(String studentId, String transcriptId, String versionId);
}
