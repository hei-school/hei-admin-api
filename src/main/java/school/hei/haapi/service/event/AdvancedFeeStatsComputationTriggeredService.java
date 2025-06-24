package school.hei.haapi.service.event;

import static java.time.ZoneOffset.UTC;
import static java.util.Optional.empty;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.AdvancedFeeStatsComputationTriggered;
import school.hei.haapi.service.AdvancedFeeStatsService;

@Service
@AllArgsConstructor
@Slf4j
public class AdvancedFeeStatsComputationTriggeredService
    implements Consumer<AdvancedFeeStatsComputationTriggered> {
  private final AdvancedFeeStatsService advancedFeeStatsService;

  @Override
  public void accept(AdvancedFeeStatsComputationTriggered advancedFeeStatsComputationTriggered) {
    log.info("Refresh advanced fee stats triggered at {}", Instant.now());
    Optional<Instant> fromValue =
        Optional.ofNullable(advancedFeeStatsComputationTriggered.getBeginDatetime().toInstant(UTC));
    Optional<Instant> toValue =
        Optional.ofNullable(advancedFeeStatsComputationTriggered.getEnd().toInstant(UTC));
    advancedFeeStatsService.updateAdvancedFeeStats(fromValue, toValue, empty());
  }
}
