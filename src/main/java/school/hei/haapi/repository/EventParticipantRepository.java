package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.dto.MissedEventStatsDto;

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

  int countByEventIdInAndStatus(List<String> eventIds, AttendanceStatus status);

  @Query(
      """
        select new school.hei.haapi.model.dto.MissedEventStatsDto(
            sum(case when e.status = 'MISSING' then 1 else 0 end),
            sum(case when size(e.letters) > 0 and e.status = 'MISSING' then 1 else 0 end),
            sum(case when size(e.letters) = 0 and e.status = 'MISSING' then 1 else 0 end))
        from EventParticipant e where e.participant.id = :studentId
      """)
  MissedEventStatsDto countMissedEventStatsByStudentId(String studentId);

  @Query(
      """
        select new school.hei.haapi.model.dto.MissedEventStatsDto(
            sum(case when e.status = 'MISSING' then 1 else 0 end),
            sum(case when size(e.letters) > 0 and e.status = 'MISSING' then 1 else 0 end),
            sum(case when size(e.letters) = 0 and e.status = 'MISSING' then 1 else 0 end))
        from EventParticipant e where e.participant.id = :studentId
        and e.event.id in :eventIds
      """)
  MissedEventStatsDto countMissedEventStatsByStudentIdAndEventIds(
      String studentId, List<String> eventIds);

  @Query(
      """
select new school.hei.haapi.model.dto.MissedEventStatsDto(
    sum(case when e is not null and e.status = 'MISSING' then 1 else 0 end),
    sum(case when e is not null and size(e.letters) > 0 and e.status = 'MISSING' then 1 else 0 end),
    sum(case when e is not null and size(e.letters) = 0 and e.status = 'MISSING' then 1 else 0 end))
from EventParticipant e where e.event.id in :eventIds
""")
  MissedEventStatsDto countMissedEventStatsByEventIds(List<String> eventIds);

  @Query(
      """
        select new school.hei.haapi.model.dto.MissedEventStatsDto(
            sum(case when e.status = 'MISSING' then 1 else 0 end),
            sum(case when size(e.letters) > 0 and e.status = 'MISSING' then 1 else 0 end),
            sum(case when size(e.letters) = 0 and e.status = 'MISSING' then 1 else 0 end))
        from EventParticipant e
      """)
  MissedEventStatsDto countOverallMissedEventStats();
}
