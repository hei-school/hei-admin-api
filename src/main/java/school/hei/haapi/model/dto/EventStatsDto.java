package school.hei.haapi.model.dto;

import school.hei.haapi.endpoint.rest.model.EventParticipantStats;
import school.hei.haapi.endpoint.rest.model.EventStats;

public record EventStatsDto(
    Long total, Long present, Long late, Long unchecked, MissedEventStatsDto missedEventStatsDto) {
  public EventStats toEventStats() {
    return new EventStats()
        .present(this.present.intValue())
        .late(this.late.intValue())
        .missedStats(missedEventStatsDto.toRest())
        .total((int) (this.present + this.late + this.missedEventStatsDto.total()));
  }

  public EventParticipantStats toEventParticipantStats() {
    return new EventParticipantStats()
        .assistedEvents(this.present.intValue())
        .lateEvents(this.late.intValue())
        .missedEvents(this.missedEventStatsDto.toRest())
        .totalEvents((int) (this.present + this.late + this.missedEventStatsDto.total()));
  }
}
