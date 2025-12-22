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
import school.hei.haapi.model.dto.LetterDto;

@Repository
public interface LetterRepository extends JpaRepository<Letter, String> {
  List<Letter> findAllByUserId(String studentId, Pageable pageable);

  List<Letter> findAllByUserIdAndStatus(String studentId, LetterStatus status, Pageable pageable);

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
      SELECT new school.hei.haapi.model.dto.LetterDto(
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
  LetterDto getLetterStatisticsForRoles(@Param("roles") List<User.Role> roles);

  @Query(
      """
      SELECT new school.hei.haapi.model.dto.LetterDto(
          new school.hei.haapi.model.dto.LetterDetailsDto(
              COALESCE(SUM(CASE WHEN l.status = 'PENDING' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN l.status = 'REJECTED' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN l.status = 'RECEIVED' THEN 1 ELSE 0 END), 0),
              COUNT(l)
          )
      )
      FROM Letter l
      """)
  LetterDto getLetterStatistics();
}
