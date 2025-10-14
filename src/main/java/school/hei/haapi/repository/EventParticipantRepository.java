package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.dto.EventStatsDto;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, String> {
  boolean existsByEventIdAndGroupIdAndParticipantId(
      String eventId, String groupId, String participantId);

  Optional<List<EventParticipant>> findAllByEventId(String eventId, Pageable pageable);

  Optional<List<EventParticipant>> findAllByEventIdAndGroupRef(
      String eventId, String groupRef, Pageable pageable);

  @Query(
      """
  select new school.hei.haapi.model.dto.EventStatsDto(
        coalesce(count(e), 0),
        coalesce(sum(case when e.status = 'PRESENT' then 1 else 0 end), 0),
        coalesce(sum(case when e.status = 'LATE' then 1 else 0 end), 0),
        coalesce(sum(case when e.status = 'UNCHECKED' then 1 else 0 end), 0),
        new school.hei.haapi.model.dto.MissedEventStatsDto(
            coalesce(sum(case when e.status = 'MISSING' then 1 else 0 end), 0),
            coalesce(sum(case when size(e.letters) > 0 and e.status = 'MISSING' then 1 else 0 end), 0),
            coalesce(sum(case when size(e.letters) = 0 and e.status = 'MISSING' then 1 else 0 end), 0))
  ) from EventParticipant e where e.event.id = :eventId
""")
  EventStatsDto countEventStatsByEventId(String eventId);

  @Query(
      """
  select new school.hei.haapi.model.dto.EventStatsDto(
        coalesce(count(e), 0),
        coalesce(sum(case when e.status = 'PRESENT' then 1 else 0 end), 0),
        coalesce(sum(case when e.status = 'LATE' then 1 else 0 end), 0),
        coalesce(sum(case when e.status = 'UNCHECKED' then 1 else 0 end), 0),
        new school.hei.haapi.model.dto.MissedEventStatsDto(
            coalesce(sum(case when e.status = 'MISSING' then 1 else 0 end), 0),
            coalesce(sum(case when size(e.letters) > 0 and e.status = 'MISSING' then 1 else 0 end), 0),
            coalesce(sum(case when size(e.letters) = 0 and e.status = 'MISSING' then 1 else 0 end), 0))
  ) from EventParticipant e where e.event.beginDatetime between :from and :to
""")
  EventStatsDto countEventStatsByEventBeginBetween(Instant from, Instant to);

  @Query(
      """
  select new school.hei.haapi.model.dto.EventStatsDto(
      coalesce(count(e), 0),
      coalesce(sum(case when e.status = 'PRESENT' then 1 else 0 end), 0),
      coalesce(sum(case when e.status = 'LATE' then 1 else 0 end), 0),
      coalesce(sum(case when e.status = 'UNCHECKED' then 1 else 0 end), 0),
      new school.hei.haapi.model.dto.MissedEventStatsDto(
          coalesce(sum(case when e.status = 'MISSING' then 1 else 0 end), 0),
          coalesce(sum(case when size(e.letters) > 0 and e.status = 'MISSING' then 1 else 0 end), 0),
          coalesce(sum(case when size(e.letters) = 0 and e.status = 'MISSING' then 1 else 0 end), 0))
  ) from EventParticipant e where e.event.beginDatetime between :from and :to
  and e.participant.id = :studentId
""")
  EventStatsDto countEventStatsByStudentIdAndEventBeginBetween(
      String studentId, Instant from, Instant to);

  @Query(
      """
  select new school.hei.haapi.model.dto.EventStatsDto(
      coalesce(count(e), 0),
      coalesce(sum(case when e.status = 'PRESENT' then 1 else 0 end), 0),
      coalesce(sum(case when e.status = 'LATE' then 1 else 0 end), 0),
      coalesce(sum(case when e.status = 'UNCHECKED' then 1 else 0 end), 0),
      new school.hei.haapi.model.dto.MissedEventStatsDto(
          coalesce(sum(case when e.status = 'MISSING' then 1 else 0 end), 0),
          coalesce(sum(case when size(e.letters) > 0 and e.status = 'MISSING' then 1 else 0 end), 0),
          coalesce(sum(case when size(e.letters) = 0 and e.status = 'MISSING' then 1 else 0 end), 0))
  ) from EventParticipant e
""")
  EventStatsDto countOverallEventStats();

  @Query(
      """
  select new school.hei.haapi.model.dto.EventStatsDto(
      coalesce(count(e), 0),
      coalesce(sum(case when e.status = 'PRESENT' then 1 else 0 end), 0),
      coalesce(sum(case when e.status = 'LATE' then 1 else 0 end), 0),
      coalesce(sum(case when e.status = 'UNCHECKED' then 1 else 0 end), 0),
      new school.hei.haapi.model.dto.MissedEventStatsDto(
          coalesce(sum(case when e.status = 'MISSING' then 1 else 0 end), 0),
          coalesce(sum(case when size(e.letters) > 0 and e.status = 'MISSING' then 1 else 0 end), 0),
          coalesce(sum(case when size(e.letters) = 0 and e.status = 'MISSING' then 1 else 0 end), 0))
  ) from EventParticipant e where e.participant.id = :studentId
""")
  EventStatsDto countEventStatsByStudentId(String studentId);
}
