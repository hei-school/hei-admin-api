package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.VerifyMobilePaymentWithVolaTriggered;
import school.hei.haapi.service.MpbsVerificationService;

@Service
@AllArgsConstructor
public class VerifyMobilePaymentWithVolaTriggeredService
    implements Consumer<VerifyMobilePaymentWithVolaTriggered> {
  private final MpbsVerificationService mpbsVerificationService;

  @Override
  public void accept(VerifyMobilePaymentWithVolaTriggered event) {
    mpbsVerificationService.verifyMobilePaymentAndSaveResultWithVola();
  }
}
