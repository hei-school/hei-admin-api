package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.model.EventParticipant;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, String> {
  boolean existsByEventIdAndGroupIdAndParticipantId(
      String eventId, String groupId, String participantId);

  Optional<List<EventParticipant>> findAllByEventId(String eventId, Pageable pageable);

  Optional<List<EventParticipant>> findAllByEventIdAndGroupRef(
      String eventId, String groupRef, Pageable pageable);

  Integer countByEventIdAndStatus(String eventId, AttendanceStatus status);

  Integer countByEventId(String eventId);

  int countByStatus(AttendanceStatus status);

  @Query(
      "select count(p.id) from EventParticipant p left join Letter l "
          + "on l.eventParticipant.id = p.id where l is null and p.event.id = :eventId")
  int countUnjustifiedMissingByEventId(String eventId);

  @Query(
      "select count(p.id) from EventParticipant p left join Letter l on p.id ="
          + " l.eventParticipant.id where l is null")
  int countUnjustifiedMissing();

  int countAllByParticipantIdAndStatus(String participantId, AttendanceStatus status);

  int countByEventIdInAndStatus(List<String> eventIds, AttendanceStatus status);

  @Query(
      value =
          "select count(p.id) from EventParticipant p left join Letter l  on l.eventParticipant.id"
              + " = p.id where l is null and p.event.id in :eventIds")
  int countUnjustifiedMissingByEventIdIn(List<String> eventIds);

  int countAllByParticipantIdAndStatusAndEventIdIn(
      String participantId, AttendanceStatus status, List<String> eventIds);
}
