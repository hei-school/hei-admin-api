package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.LetterStatus;
import school.hei.haapi.model.Letter;
import school.hei.haapi.model.User;

@Repository
public interface LetterRepository extends JpaRepository<Letter, String> {
  List<Letter> findAllByUserId(String studentId, Pageable pageable);

  List<Letter> findAllByUserIdAndStatus(String studentId, LetterStatus status, Pageable pageable);

  Integer countByStatus(LetterStatus status);

  Integer countByStatusAndUserRole(LetterStatus status, User.Role userRole);

  @Query(value = "SELECT * FROM  letter where fee_id = ?1", nativeQuery = true)
  Optional<Letter> findByFeeId(String feeId);

  @Query(
      value =
          "SELECT * FROM  letter where event_participant_id = ?1 ORDER BY creation_datetime DESC",
      nativeQuery = true)
  Optional<List<Letter>> findByEventParticipantId(String eventParticipantId);

  @Query(
      value =
          "SELECT count(p.event.id) from Letter l inner join EventParticipant p "
              + "where l.eventParticipant is not null and p.event.id in :eventId group by p.id")
  int countByEventIdInUnique(List<String> eventId);

  @Query(
      value =
          "SELECT count(p.event.id) from Letter l inner join EventParticipant p "
              + "where l.eventParticipant is not null group by p.id")
  int countByEventUnique();
}
