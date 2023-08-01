package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.StudentTranscriptVersion;

@Repository
public interface StudentTranscriptVersionRepository extends JpaRepository<StudentTranscriptVersion, String> {
    @Query(value = "SELECT v FROM StudentTranscriptVersion v WHERE v.id = :version_id"
            + " AND v.transcript.id = :transcript_id"
            + " AND v.transcript.student.id = :student_id")
    StudentTranscriptVersion getByIdAndStudentIdAndTranscriptId(
            @Param("version_id") String versionId,
            @Param("transcript_id") String transcriptId,
            @Param("student_id") String studentId);

}
