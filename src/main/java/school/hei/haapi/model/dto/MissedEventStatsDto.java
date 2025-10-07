package school.hei.haapi.model.dto;

import school.hei.haapi.endpoint.rest.model.MissedEventStats;

public record MissedEventStatsDto(Long total, Long justifiedAbsence, Long unjustifiedAbsence) {
  public MissedEventStats toRest() {
    return new MissedEventStats()
        .justified(this.justifiedAbsence.intValue())
        .unjustified(this.unjustifiedAbsence.intValue())
        .total(this.total.intValue());
  }
}
