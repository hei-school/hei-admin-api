package school.hei.haapi.model.dto;

import school.hei.haapi.endpoint.rest.model.EventParticipantStats;
import school.hei.haapi.endpoint.rest.model.EventStats;

public record EventStatsDto(
    long total, long present, long late, long unchecked, MissedEventStatsDto missedEventStatsDto) {
  public EventStats toEventStats() {
    return new EventStats()
        .present(this.present)
        .late(this.late)
        .missedStats(missedEventStatsDto.toRest())
        .total(this.present + this.late + this.missedEventStatsDto.total());
  }

  public EventParticipantStats toEventParticipantStats() {
    return new EventParticipantStats()
        .assistedEvents(this.present)
        .lateEvents(this.late)
        .missedEvents(this.missedEventStatsDto.toRest())
        .totalEvents(this.present + this.late + this.missedEventStatsDto.total());
  }
}
