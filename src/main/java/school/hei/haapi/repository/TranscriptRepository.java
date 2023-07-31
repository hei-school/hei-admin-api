package school.hei.haapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.Transcript;

import java.util.List;

@Repository
public interface TranscriptRepository extends JpaRepository<Transcript, String> {
    @Query(value = "SELECT t FROM Transcript t WHERE t.id = :transcript_id"
            + " AND t.student.id = :student_id")
    Transcript getTranscriptByIdAndStudentId(
            @Param("transcript_id") String transcriptId,
            @Param("student_id") String studentId);

    @Query(value = "SELECT t FROM Transcript t WHERE t.student.id = :student_id")
    List<Transcript> getAllTranscriptsByStudentId(
            @Param("student_id") String studentId, Pageable pageable);

    List<Transcript> getAllByStudentId(String studentId);
}
