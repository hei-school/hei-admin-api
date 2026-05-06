package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.LetterStatus;
import school.hei.haapi.model.Letter;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.letterStatsDto;

@Repository
public interface LetterRepository extends JpaRepository<Letter, String> {
  @Query(
      "SELECT l FROM Letter l WHERE l.user.id = :studentId "
          + "AND (:eventParticipantId IS NULL OR l.eventParticipant.id = :eventParticipantId)")
  List<Letter> findAllByUserIdAndEventParticipantId(
      @Param("studentId") String studentId,
      @Param("eventParticipantId") String eventParticipantId,
      Pageable pageable);

  @Query(
      """
      SELECT l FROM Letter l
      WHERE l.user.id = :studentId
      AND l.status = :status
      AND (:eventParticipantId IS NULL OR l.eventParticipant.id = :eventParticipantId)
      """)
  List<Letter> findAllByUserIdAndStatusAndEventParticipantId(
      @Param("studentId") String studentId,
      @Param("status") LetterStatus status,
      @Param("eventParticipantId") String eventParticipantId,
      Pageable pageable);

  @Query("SELECT l FROM Letter l WHERE l.fee.id = :feeId")
  Optional<Letter> findByFeeId(@Param("feeId") String feeId);

  @Query(
      """
      SELECT l
      FROM Letter l
      WHERE l.eventParticipant.id = :eventParticipantId
      ORDER BY l.creationDatetime DESC
      """)
  List<Letter> findByEventParticipantId(@Param("eventParticipantId") String eventParticipantId);

  @Query(
      """
      SELECT new school.hei.haapi.model.dto.letterStatsDto(
          new school.hei.haapi.model.dto.LetterDetailsDto(
              COALESCE(SUM(CASE WHEN l.status = 'PENDING' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN l.status = 'REJECTED' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN l.status = 'RECEIVED' THEN 1 ELSE 0 END), 0),
              COUNT(l)
          )
      )
      FROM Letter l
      WHERE l.user.role IN :roles
      """)
  letterStatsDto getLetterStatisticsForRoles(@Param("roles") List<User.Role> roles);

  @Query(
      """
      SELECT new school.hei.haapi.model.dto.letterStatsDto(
          new school.hei.haapi.model.dto.LetterDetailsDto(
              COALESCE(SUM(CASE WHEN l.status = 'PENDING' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN l.status = 'REJECTED' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN l.status = 'RECEIVED' THEN 1 ELSE 0 END), 0),
              COUNT(l)
          )
      )
      FROM Letter l
      """)
  letterStatsDto getLetterStatistics();
}
