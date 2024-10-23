package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.LetterStatus;
import school.hei.haapi.model.Letter;

@Repository
public interface LetterRepository extends JpaRepository<Letter, String> {
  List<Letter> findAllByStudentId(String studentId, Pageable pageable);

  List<Letter> findAllByStudentIdAndStatus(
      String studentId, LetterStatus status, Pageable pageable);

  Integer countByStatus(LetterStatus status);

  @Query(value = "SELECT * FROM  letter where fee_id = ?1", nativeQuery = true)
  Optional<Letter> findByFeeId(String feeId);

  @Query(
      value =
          "SELECT * FROM  letter where event_participant_id = ?1 ORDER BY creation_datetime DESC",
      nativeQuery = true)
  Optional<List<Letter>> findByEventParticipantId(String eventParticipantId);
}
