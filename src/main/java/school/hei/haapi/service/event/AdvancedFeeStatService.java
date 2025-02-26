package school.hei.haapi.service.event;

import java.time.Instant;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.AdvancedFeeStatsTriggered;
import school.hei.haapi.service.FeeService;

@Service
@AllArgsConstructor
@Slf4j
public class AdvancedFeeStatService implements Consumer<AdvancedFeeStatsTriggered> {
  private final FeeService feeService;

  @Override
  public void accept(AdvancedFeeStatsTriggered advancedFeeStatsTriggered) {
    log.info("Refresh advanced fee stats triggered at {}", Instant.now());
    feeService.updateAdvancedFeeStats();
  }
}
