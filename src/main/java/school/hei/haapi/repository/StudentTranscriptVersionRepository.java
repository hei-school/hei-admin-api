package school.hei.haapi.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.StudentTranscriptVersion;
import school.hei.haapi.model.Transcript;

import java.util.List;

@Repository
public interface StudentTranscriptVersionRepository extends JpaRepository<StudentTranscriptVersion, String> {
    @Query(value = "SELECT v FROM StudentTranscriptVersion v WHERE v.id = :version_id"
            + " AND v.transcript.id = :transcript_id"
            + " AND v.transcript.student.id = :student_id")
    StudentTranscriptVersion getByIdAndStudentIdAndTranscriptId(
            @Param("version_id") String versionId,
            @Param("transcript_id") String transcriptId,
            @Param("student_id") String studentId);

    @Query(value = "SELECT v FROM StudentTranscriptVersion v WHERE v.transcript.id = :transcript_id"
            + " AND v.transcript.student.id = :student_id")
    List<StudentTranscriptVersion> getAllByStudentIdAndTranscriptId(
            @Param("student_id") String studentId,
            @Param("transcript_id") String transcriptId,
            Pageable pageable);

}
