package school.hei.haapi.model.dto;

import school.hei.haapi.endpoint.rest.model.LetterStats;

public record letterStatsDto(LetterDetailsDto details) {

  public LetterStats toLetterStats() {
    return new LetterStats()
        .pending((int) details.pending())
        .rejected((int) details.rejected())
        .received((int) details.received());
  }
}
