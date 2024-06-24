package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.gen.FetchMobileTransactionTriggered;
import school.hei.haapi.service.MpbsVerificationService;

@Service
@AllArgsConstructor
public class FetchMobileTransactionTriggeredService
    implements Consumer<FetchMobileTransactionTriggered> {
  private final MpbsVerificationService mpbsVerificationService;

  @Override
  public void accept(FetchMobileTransactionTriggered fetchMobileTransactionTriggered) {
    mpbsVerificationService.fetchThenSaveTransactionDetailsDaily();
  }
}
