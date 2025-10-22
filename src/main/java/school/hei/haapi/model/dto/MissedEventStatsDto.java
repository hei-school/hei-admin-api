package school.hei.haapi.model.dto;

import school.hei.haapi.endpoint.rest.model.MissedEventStats;

public record MissedEventStatsDto(long total, long justifiedAbsence, long unjustifiedAbsence) {
  public MissedEventStats toRest() {
    return new MissedEventStats()
        .justified(this.justifiedAbsence)
        .unjustified(this.unjustifiedAbsence)
        .total(this.total);
  }
}
