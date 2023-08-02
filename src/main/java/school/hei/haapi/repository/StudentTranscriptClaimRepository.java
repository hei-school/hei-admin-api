package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.StudentTranscriptClaim;
import school.hei.haapi.model.StudentTranscriptVersion;

import java.util.List;

@Repository
public interface StudentTranscriptClaimRepository extends JpaRepository<StudentTranscriptClaim, String> {
    @Query(value = "SELECT c FROM StudentTranscriptClaim c WHERE c.id = :claim_id"
            + " AND c.transcriptVersion.id = :version_id"
            + " AND c.transcript.id = :transcript_id"
            + " AND c.transcript.student.id = :student_id")
    StudentTranscriptClaim getByIdAndStudentIdAndTranscriptIdAndVersionId(
            @Param("claim_id") String claimId,
            @Param("version_id") String versionId,
            @Param("transcript_id") String transcriptId,
            @Param("student_id") String studentId);

    @Query(value = "SELECT c FROM StudentTranscriptVersion c WHERE c.id = :version_id"
            + " AND c.transcript.id = :transcript_id"
            + " AND c.transcript.student.id = :student_id")
    List<StudentTranscriptClaim> getAllByStudentIdAndTranscriptIdAndVersionId(
            @Param("version_id") String versionId,
            @Param("transcript_id") String transcriptId,
            @Param("student_id") String studentId);
}
