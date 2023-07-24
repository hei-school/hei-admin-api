package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.TranscriptVersion;

import java.util.List;

@Repository
public interface TranscriptVersionRepository extends JpaRepository<TranscriptVersion,String> {
    List<TranscriptVersion> getAllByUserIdAndTranscriptId(String userId,String transcriptId,Pageable pageable);
    TranscriptVersion getTranscriptVersionByUserIdAndTranscriptIdAndId(String userId, String transcriptId,String versionId);

    @Query(value = "select t from TranscriptVersion t"
            + " where t.user.id = :user_id and t.transcript.id = :transcript_id"
            + " order by t.ref DESC")
    List<TranscriptVersion> getAllSortedByRef(
            @Param("user_id") String userId,
            @Param("transcript_id") String transcriptId);
}
