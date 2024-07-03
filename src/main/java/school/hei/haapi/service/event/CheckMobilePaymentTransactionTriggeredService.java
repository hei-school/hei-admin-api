package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.CheckMobilePaymentTransactionTriggered;
import school.hei.haapi.service.MpbsVerificationService;

@Service
@AllArgsConstructor
public class CheckMobilePaymentTransactionTriggeredService
    implements Consumer<CheckMobilePaymentTransactionTriggered> {
  private final MpbsVerificationService mpbsVerificationService;

  @Override
  public void accept(
      CheckMobilePaymentTransactionTriggered checkMobilePaymentTransactionTriggered) {
    mpbsVerificationService.checkMobilePaymentThenSaveVerification();
  }
}
