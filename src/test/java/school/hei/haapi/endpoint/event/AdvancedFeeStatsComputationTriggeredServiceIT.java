package school.hei.haapi.endpoint.event;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.model.AdvancedFeeStatsComputationTriggered;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.service.AdvancedFeeStatsService;
import school.hei.haapi.service.event.AdvancedFeeStatsComputationTriggeredService;

class AdvancedFeeStatsComputationTriggeredServiceIT extends FacadeITMockedThirdParties {
  @Autowired AdvancedFeeStatsComputationTriggeredService subject;

  @MockBean AdvancedFeeStatsService service;

  @Test
  void trigger_advanced_fee_stats_computation_ok() {
    assertDoesNotThrow(() -> subject.accept(new AdvancedFeeStatsComputationTriggered()));
    assertDoesNotThrow(
        () ->
            subject.accept(
                new AdvancedFeeStatsComputationTriggered(
                    LocalDateTime.now(), LocalDateTime.now(), empty())));
    verify(service, times(2)).updateAdvancedFeeStats(any(), any(), any());
  }
}
