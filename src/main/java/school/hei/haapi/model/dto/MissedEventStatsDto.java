package school.hei.haapi.model.dto;

import school.hei.haapi.endpoint.rest.model.MissedEventStats;

public record MissedEventStatsDto(Long total, Long justifiedAbsence, Long unjustifiedAbsence) {
  public MissedEventStats toRest() {
    return new MissedEventStats()
        .justified(this.justifiedAbsence == null ? 0 : this.justifiedAbsence.intValue())
        .unjustified(this.unjustifiedAbsence == null ? 0 : this.unjustifiedAbsence.intValue())
        .total(this.total == null ? 0 : this.total.intValue());
  }
}
